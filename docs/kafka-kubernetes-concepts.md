# Kafka on Kubernetes — Concept Revision Guide

This guide summarizes the core concepts discussed in this session, up to (but excluding) the later discussion about large-scale industry production infrastructure.

The goal is not to be exhaustive. It is designed to be easy to revisit and quickly rebuild the mental model.

---

## 1. The Most Important Distinction

A **Kubernetes cluster** and a **Kafka cluster** are different concepts.

```text
Kubernetes Cluster
│
│  runs workloads
│
├── kafka-0 Pod
│      └── Kafka Broker 0
│
├── kafka-1 Pod
│      └── Kafka Broker 1
│
└── kafka-2 Pod
       └── Kafka Broker 2

These Kafka brokers together form:

        Kafka Cluster
```

### Kubernetes cluster

Kubernetes manages:

- Pods
- containers
- networking
- DNS
- storage
- scheduling
- restart/recovery of workloads

### Kafka cluster

Kafka manages:

- brokers
- controllers
- topics
- partitions
- leaders
- followers
- replicas
- replication
- metadata
- producers and consumers interacting with the cluster

A single Kubernetes cluster can host more than one completely independent Kafka cluster.

---

# 2. Container → Pod → Broker

The layering is:

```text
Kubernetes Worker Node
        │
        ▼
       Pod
        │
        ▼
    Container
        │
        ▼
 Kafka JVM Process
        │
        ▼
  Kafka Broker
```

A Kafka broker is ultimately just a running Kafka process.

Kubernetes does not directly create a “Kafka broker.”

Kubernetes creates a Pod and container.

The Kafka process running inside the container becomes the broker.

---

# 3. Why Use a StatefulSet?

Suppose we want three Kafka brokers.

A simplified StatefulSet:

```yaml
apiVersion: apps/v1
kind: StatefulSet

metadata:
  name: kafka

spec:
  serviceName: kafka-headless
  replicas: 3

  selector:
    matchLabels:
      app: kafka

  template:
    metadata:
      labels:
        app: kafka

    spec:
      containers:
        - name: kafka
          image: apache/kafka:...
```

Because this is a StatefulSet, Kubernetes creates predictable Pod names:

```text
kafka-0
kafka-1
kafka-2
```

This stable identity is very useful for Kafka.

Compare this with ordinary stateless Pods, where names may be random.

---

# 4. Why Pod IP Addresses Are Not Enough

Each Pod gets an IP address.

For example:

```text
kafka-0 → 10.244.1.17
kafka-1 → 10.244.2.24
kafka-2 → 10.244.3.31
```

Kafka brokers can communicate over normal TCP networking.

For example:

```text
kafka-0
   │
   │ TCP
   ▼
10.244.2.24:9092
```

But Pod IP addresses are not stable identities.

If `kafka-1` crashes, Kubernetes may recreate it with another IP:

```text
Before:
kafka-1 → 10.244.2.24

After restart:
kafka-1 → 10.244.5.92
```

Kafka therefore should not rely on hardcoded Pod IPs.

---

# 5. Headless Service: Stable DNS for Brokers

Create a Headless Service:

```yaml
apiVersion: v1
kind: Service

metadata:
  name: kafka-headless

spec:
  clusterIP: None

  selector:
    app: kafka

  ports:
    - name: broker
      port: 9092
```

The important line:

```yaml
clusterIP: None
```

This makes it a Headless Service.

The StatefulSet references it:

```yaml
spec:
  serviceName: kafka-headless
```

Now each StatefulSet Pod gets a stable DNS identity such as:

```text
kafka-0.kafka-headless
kafka-1.kafka-headless
kafka-2.kafka-headless
```

Potential full names:

```text
kafka-0.kafka-headless.default.svc.cluster.local
kafka-1.kafka-headless.default.svc.cluster.local
kafka-2.kafka-headless.default.svc.cluster.local
```

So from `kafka-0`, Kafka can reach Broker 1 through:

```text
kafka-1.kafka-headless:9092
```

The sequence is:

```text
DNS name
   ↓
Kubernetes DNS lookup
   ↓
Current Pod IP
   ↓
TCP connection
   ↓
Kafka protocol
```

This is the basis of inter-broker networking in Kubernetes.

---

# 6. Kafka Listener Configuration

Kafka itself has to listen for connections.

Conceptually:

```properties
listeners=INTERNAL://0.0.0.0:9092
```

Meaning:

> Accept Kafka connections on port 9092.

But Kafka also needs to tell other brokers and clients how they should reach it.

That is the role of `advertised.listeners`.

For Broker 0:

```properties
advertised.listeners=INTERNAL://kafka-0.kafka-headless:9092
```

For Broker 1:

```properties
advertised.listeners=INTERNAL://kafka-1.kafka-headless:9092
```

For Broker 2:

```properties
advertised.listeners=INTERNAL://kafka-2.kafka-headless:9092
```

And Kafka can use an internal listener for broker-to-broker communication:

```properties
inter.broker.listener.name=INTERNAL
```

### Mental model

```text
listeners
=
"Where am I listening?"

advertised.listeners
=
"What address should others use to reach me?"
```

---

# 7. How One StatefulSet Produces Unique Brokers

There is only one StatefulSet template, but each Pod has a different name.

Kubernetes can expose the Pod name:

```yaml
env:
  - name: POD_NAME
    valueFrom:
      fieldRef:
        fieldPath: metadata.name
```

Then:

```text
inside kafka-0:
POD_NAME=kafka-0

inside kafka-1:
POD_NAME=kafka-1

inside kafka-2:
POD_NAME=kafka-2
```

The startup process can derive the advertised address:

```text
${POD_NAME}.kafka-headless:9092
```

giving:

```text
kafka-0.kafka-headless:9092
kafka-1.kafka-headless:9092
kafka-2.kafka-headless:9092
```

The ordinal can also be used for the Kafka node ID:

```text
kafka-0 → node.id=0
kafka-1 → node.id=1
kafka-2 → node.id=2
```

The brokers therefore have:

- one common Kafka cluster identity
- unique node identities
- unique stable DNS addresses

---

# 8. Kafka Cluster Is Separate from Kubernetes Cluster

The three broker Pods do not become a Kafka cluster merely because they belong to one StatefulSet.

Kafka has its own cluster identity and control plane.

Conceptually:

```text
Kubernetes Cluster
│
├── kafka-0 → Kafka node 0
├── kafka-1 → Kafka node 1
└── kafka-2 → Kafka node 2

        ↓

All configured as members of:

Kafka Cluster ABC123
```

Modern Kafka uses KRaft rather than ZooKeeper.

---

# 9. Kafka Cluster ID

A new Kafka cluster gets one cluster ID.

Example command:

```bash
bin/kafka-storage.sh random-uuid
```

Example result:

```text
V9x123abc
```

This ID is generated **once when initializing a brand-new Kafka cluster**.

It is not regenerated every time a broker restarts.

All nodes belonging to that Kafka cluster use the same cluster ID:

```text
kafka-0 → cluster ID V9x123abc
kafka-1 → cluster ID V9x123abc
kafka-2 → cluster ID V9x123abc
```

Their `node.id` values are different:

```text
kafka-0 → node.id=0
kafka-1 → node.id=1
kafka-2 → node.id=2
```

---

# 10. Who Generates the Cluster ID?

It depends on how Kafka is deployed.

In a manual setup:

- infrastructure engineer
- deployment script
- automation pipeline

may generate it once.

Example:

```bash
CLUSTER_ID=$(bin/kafka-storage.sh random-uuid)
```

In Kubernetes, that value can be stored in a Kubernetes Secret or other configuration source and provided to all Kafka Pods.

Conceptually:

```text
Kubernetes Secret
kafka-cluster-id
       │
       │ V9x123abc
       │
 ┌─────┼─────┐
 ▼     ▼     ▼

kafka-0
kafka-1
kafka-2
```

With an operator such as Strimzi, much of this lifecycle is automated.

---

# 11. Formatting Kafka Storage

A brand-new Kafka node's storage must be initialized.

Conceptually:

```bash
bin/kafka-storage.sh format \
  --cluster-id V9x123abc \
  --config /etc/kafka/server.properties
```

The process initializes the Kafka storage with cluster/node metadata.

In Kubernetes this can be handled by:

- an init container
- an entrypoint/startup script
- an operator

Example lifecycle:

```text
Pod starts
   ↓
init/startup logic
   ↓
Is storage new?
   │
   ├── yes → format once
   │
   └── no  → keep existing storage
   ↓
start Kafka broker
```

A normal broker restart must **not create a new cluster ID**.

---

# 12. Persistent Storage

Each broker should have its own persistent storage.

A StatefulSet may use:

```yaml
volumeClaimTemplates:
  - metadata:
      name: kafka-data
    spec:
      accessModes:
        - ReadWriteOnce
      resources:
        requests:
          storage: 20Gi
```

This produces storage such as:

```text
kafka-0
   └── PVC kafka-data-kafka-0

kafka-1
   └── PVC kafka-data-kafka-1

kafka-2
   └── PVC kafka-data-kafka-2
```

If `kafka-1` crashes:

```text
old kafka-1 Pod
      💥
       │
       ▼
PVC survives
       │
       ▼
new kafka-1 Pod
       │
       └── mounts the same broker data
```

Stable Pod identity + stable storage identity are two major reasons StatefulSets fit Kafka.

---

# 13. What Makes the Brokers Operate as One Kafka Cluster?

A more complete picture is:

```text
Same Kafka cluster ID
        +
unique node IDs
        +
network connectivity
        +
KRaft controller configuration
        +
Kafka cluster metadata
        ↓

One Kafka cluster
```

The cluster ID alone is not enough.

The brokers/controllers must also participate in the same KRaft-based Kafka control plane.

---

# 14. KRaft: The Kafka Control Plane

Modern Kafka uses KRaft to manage cluster metadata.

Kafka needs to know things like:

```text
Which brokers exist?
Which topics exist?
How many partitions?
Where are replicas?
Who is the leader for each partition?
What broker is alive?
```

Conceptually:

```text
             KRaft Controllers
                    │
                    │ cluster metadata
                    ▼

        ┌───────────┼───────────┐
        ▼           ▼           ▼
     Broker 0    Broker 1    Broker 2
```

Small test setups may combine broker and controller roles.

Larger setups may separate them.

The important concept here is:

> Kafka's control plane knows the structure of the Kafka cluster.

---

# 15. When Are Topics Created?

Only after the Kafka cluster is up and operational do you create application topics.

Example:

```text
Kafka brokers are running
        ↓
KRaft/control plane is operational
        ↓
Now create topic "orders"
```

A topic belongs to Kafka infrastructure.

It does **not** live inside your producer or consumer application.

---

# 16. Where Does `kafka-topics.sh` Come From?

`kafka-topics.sh` is included in the Apache Kafka distribution.

Kafka includes CLI utilities such as:

```text
bin/
├── kafka-topics.sh
├── kafka-console-producer.sh
├── kafka-console-consumer.sh
├── kafka-consumer-groups.sh
├── kafka-configs.sh
└── kafka-storage.sh
```

Inside a Kafka container image, these scripts are usually already available.

The exact filesystem path depends on the image.

You can run the command:

- from a Kafka Pod using `kubectl exec`
- from another admin/debug Pod containing Kafka tools
- from your local machine if it has Kafka CLI tools and network access

---

# 17. Creating a Topic

Example:

```bash
kafka-topics.sh \
  --bootstrap-server kafka-0.kafka-headless:9092 \
  --create \
  --topic orders \
  --partitions 4 \
  --replication-factor 3
```

This means:

```text
Create:

topic = orders
partitions = 4
replication factor = 3
```

Important:

> This does NOT mean “create the topic on kafka-0.”

`kafka-0.kafka-headless:9092` is only an initial entry point into the Kafka cluster.

The topic belongs to the cluster.

---

# 18. Meaning of Bootstrap Server

`--bootstrap-server` means:

> Connect here first so I can discover the Kafka cluster.

Conceptually:

```text
kafka-topics.sh
       │
       │ initial connection
       ▼
    kafka-0
       │
       │ cluster metadata
       ▼

Broker 0
Broker 1
Broker 2
```

A client may be configured with several bootstrap brokers for resilience.

But after connecting, it learns the cluster metadata.

---

# 19. Topic, Partition, Replica

Suppose:

```text
Topic: orders
Partitions: 4
Replication Factor: 3
```

Kafka creates four logical partitions:

```text
orders-0
orders-1
orders-2
orders-3
```

`RF = 3` means:

> Each individual partition has three replicas on three distinct brokers.

This does **not** mean the whole topic simply chooses three brokers once.

Replication is **per partition**.

---

# 20. Example: 3 Brokers, 4 Partitions, RF=3

Brokers:

```text
Broker 0
Broker 1
Broker 2
```

Topic:

```text
orders
partitions = 4
RF = 3
```

Total partition replicas:

```text
4 partitions × 3 replicas
= 12 replica instances
```

Because there are exactly three brokers and RF is 3, every partition exists on every broker.

One possible leader assignment:

```text
             Broker 0      Broker 1      Broker 2
             --------      --------      --------

orders P0    LEADER        follower      follower

orders P1    follower      LEADER        follower

orders P2    follower      follower      LEADER

orders P3    LEADER        follower      follower
```

Each partition has:

- exactly one leader
- two followers

Kafka spreads leadership across brokers.

Because there are 4 leaders but only 3 brokers, one broker will necessarily lead more than one partition.

That is completely normal.

---

# 21. Example: 3 Brokers, 4 Partitions, RF=2

Now:

```text
Brokers = 3
Partitions = 4
RF = 2
```

Total replicas:

```text
4 × 2 = 8
```

Possible assignment:

```text
             Broker 0      Broker 1      Broker 2
             --------      --------      --------

P0           LEADER        follower

P1                         LEADER        follower

P2           follower                    LEADER

P3           LEADER                      follower
```

Equivalent view:

```text
P0 → Broker 0 + Broker 1
P1 → Broker 1 + Broker 2
P2 → Broker 2 + Broker 0
P3 → Broker 0 + Broker 2
```

Very important:

> RF=2 means two distinct brokers **for each partition**.

It does not mean the entire topic uses only two brokers.

All three brokers may participate across different partitions.

---

# 22. Relationship Between Broker Count and Replication Factor

Replication factor cannot be higher than the number of available brokers.

With three brokers:

```text
RF=1 ✅
RF=2 ✅
RF=3 ✅
RF=4 ❌
```

Because Kafka cannot place four replicas of the same partition on only three distinct brokers.

---

# 23. Where Do Partition Replicas Physically Live?

They live on Kafka broker storage.

For example, with:

```text
orders
partitions = 4
RF = 3
brokers = 3
```

Broker storage may conceptually contain:

```text
Broker 0 PVC
├── orders-0
├── orders-1
├── orders-2
└── orders-3

Broker 1 PVC
├── orders-0
├── orders-1
├── orders-2
└── orders-3

Broker 2 PVC
├── orders-0
├── orders-1
├── orders-2
└── orders-3
```

Because every partition has three replicas and there are exactly three brokers.

Only one replica of each partition is the leader.

---

# 24. Producer Responsibility vs Kafka Responsibility

The producer does not manage replication.

A producer roughly says:

```text
"Send this record to topic orders."
```

Potentially with a key:

```java
new ProducerRecord<>(
    "orders",
    "customer-123",
    orderJson
);
```

Kafka/client metadata then determines which partition leader should receive the record.

### Producer/app responsibility

- topic name
- message key/value
- serialization
- producer settings
- business logic

### Kafka infrastructure responsibility

- topic existence
- partition count
- replica placement
- leader/follower assignment
- replication
- broker failure handling
- cluster metadata

---

# 25. Consumer Responsibility

Consumers also do not manually decide where replicas live.

A consumer connects to Kafka, receives metadata, and reads the appropriate partition leaders.

A consumer group ID is normally application configuration, for example:

```text
group.id=payment-service
```

Kafka then coordinates which partitions are assigned to which consumers in that group.

---

# 26. How Replication Actually Works

Suppose:

```text
orders partition 0

Leader   = Broker 0
Follower = Broker 1
Follower = Broker 2
```

Producer sends a record to the leader:

```text
Producer
   │
   ▼
Broker 0
P0 LEADER
```

Followers replicate by fetching data from the leader.

Conceptually:

```text
Broker 1:
"Give me records after offset 100."

Broker 0:
"Here are 101, 102, 103."

Broker 2:
"Give me records after offset 102."

Broker 0:
"Here is 103."
```

The leader does not normally “push” all records to followers.

Followers fetch from the leader.

---

# 27. Inter-Broker Networking vs Topic Creation

Do not mix these two concepts.

## Networking comes first

Before creating any topic:

```text
kafka-0
kafka-1
kafka-2

already:
- can reach each other
- have stable DNS
- have listeners
- participate in one Kafka/KRaft cluster
```

## Topic creation happens later

Then an admin creates:

```text
orders
partitions=4
RF=3
```

Kafka's control plane decides:

- which brokers host each partition replica
- which replica is the leader
- which replicas are followers

So:

```text
Headless Service + networking
=
"Can brokers reach one another?"

Kafka cluster/control plane
=
"Who are the brokers and what metadata exists?"

Topic creation
=
"Create partitions and assign replicas."

Replication
=
"Followers continuously fetch partition data from leaders."
```

---

# 28. What Happens When a Broker Pod Restarts?

Suppose:

```text
kafka-1
DNS = kafka-1.kafka-headless
IP  = 10.244.2.24
```

Pod crashes.

New Pod may become:

```text
kafka-1
DNS = kafka-1.kafka-headless
IP  = 10.244.5.92
```

Important stable properties remain:

```text
Pod identity:
kafka-1

DNS:
kafka-1.kafka-headless

Storage:
kafka-data-kafka-1
```

DNS is updated to the new Pod IP.

Other brokers continue referring to the stable DNS name.

That is why stable DNS + StatefulSet + persistent storage are so important.

---

# 29. The Full Setup Flow

Keep this sequence in mind.

```text
1. Kubernetes cluster exists
        ↓

2. Create Headless Service
        ↓
   stable DNS namespace for Kafka Pods

3. Create StatefulSet
        ↓
   kafka-0
   kafka-1
   kafka-2

4. Attach persistent storage
        ↓
   PVC-0
   PVC-1
   PVC-2

5. Initialize Kafka storage
        ↓
   one shared Kafka cluster ID
   unique node IDs

6. Configure listeners/DNS
        ↓
   brokers can reach each other

7. Start KRaft control plane
        ↓
   Kafka cluster metadata becomes operational

8. Brokers operate as one Kafka cluster
        ↓

9. Create topic
        ↓
   orders
   partitions=4
   RF=3

10. Kafka assigns leaders/followers
        ↓

11. Producer writes to partition leaders
        ↓

12. Followers replicate
        ↓

13. Consumers read from Kafka
```

---

# 30. One Compact Mental Model

```text
KUBERNETES
│
├── StatefulSet
│      ├── kafka-0
│      ├── kafka-1
│      └── kafka-2
│
├── Headless Service
│      └── stable broker DNS
│
└── PVCs
       └── durable broker data


KAFKA
│
├── one Kafka cluster
│
├── KRaft metadata/control plane
│
├── Broker 0
├── Broker 1
└── Broker 2
       │
       ▼

Topic: orders
│
├── P0
├── P1
├── P2
└── P3

Each partition:
│
├── one leader
└── follower replicas
```

---

# FAQ

## Is a Kafka cluster the same as a Kubernetes cluster?

No.

Kubernetes is the platform running the Kafka processes.

Kafka has its own independent concept of a cluster.

---

## Does a StatefulSet itself connect Kafka brokers?

No.

A StatefulSet gives stable Pod identities and storage behavior.

The Headless Service + Kubernetes networking provide stable reachability.

Kafka listener/KRaft configuration makes the Kafka processes communicate as a Kafka cluster.

---

## Does creating a topic establish broker networking?

No.

Broker networking and Kafka cluster formation already exist before topic creation.

Topic creation establishes logical topic/partition/replica assignments inside the already-running Kafka cluster.

---

## Does `--bootstrap-server kafka-0...` mean the topic lives on kafka-0?

No.

It is just the initial Kafka server used to enter/discover the Kafka cluster.

The topic is cluster-wide.

---

## Is the topic inside the producer application?

No.

The topic is Kafka infrastructure.

The producer simply writes to it.

---

## Is replication factor a property of the producer?

No.

Replication factor is a Kafka topic/infrastructure property.

---

## With 3 brokers and RF=2, are only 2 brokers used?

Not necessarily.

Each **partition** gets 2 broker replicas.

Different partitions may use different pairs, so all 3 brokers can participate in the overall topic.

---

## With 3 brokers, 4 partitions, RF=3, what happens?

There are:

```text
4 × 3 = 12 partition replicas
```

Every partition has one replica on all three brokers.

There are four partition leaders spread across the three brokers.

---

## Can RF be greater than broker count?

No.

For a partition, each replica must live on a different broker.

So:

```text
RF <= number of brokers
```

---

## Who creates the Kafka cluster ID?

For a brand-new Kafka cluster, deployment/infrastructure automation generates it once.

In a manual setup:

```bash
kafka-storage.sh random-uuid
```

All nodes in the Kafka cluster use the same cluster ID.

Operators may automate this.

---

## Is the cluster ID recreated on restart?

No.

The same initialized storage and cluster identity are reused.

---

## Where does `kafka-topics.sh` come from?

It is part of the Kafka distribution and is usually included in Kafka container images.

It can be run from any machine/Pod with Kafka CLI tools and network access to the cluster.

---

## What exactly is inter-broker communication?

At the lowest level:

```text
Broker DNS name
      ↓
Kubernetes DNS
      ↓
Pod IP
      ↓
TCP connection
      ↓
Kafka protocol
```

Kafka uses this connectivity for broker/controller communication and partition replication.

---

# Final Revision Cheat Sheet

```text
StatefulSet
=
stable Kafka Pod identities

Headless Service
=
stable broker DNS

PVC
=
stable broker data

listeners
=
where Kafka listens

advertised.listeners
=
how Kafka tells others to reach it

node.id
=
unique Kafka node identity

cluster ID
=
identity of the Kafka cluster/storage

KRaft
=
Kafka metadata/control plane

topic
=
named Kafka data stream/log

partition
=
shard of a topic

leader
=
active replica handling that partition's writes

follower
=
replica that fetches data from the leader

replication factor
=
number of copies of each partition

bootstrap server
=
initial entry point used to discover Kafka cluster metadata
```

---

## The Single Diagram to Remember

```text
                    Kubernetes Cluster
                           │
             ┌─────────────┼─────────────┐
             │             │             │
             ▼             ▼             ▼

          kafka-0       kafka-1       kafka-2
             │             │             │
          Broker 0      Broker 1      Broker 2
             │             │             │
             └────── Kafka Cluster ──────┘
                           │
                           │ KRaft metadata
                           ▼

                      Topic: orders

                 P0    P1    P2    P3

Example RF=3:

P0 → B0 leader + B1 follower + B2 follower
P1 → B1 leader + B2 follower + B0 follower
P2 → B2 leader + B0 follower + B1 follower
P3 → B0 leader + B1 follower + B2 follower
```

If this diagram is clear, most of the surrounding Kafka/Kubernetes architecture becomes much easier to reason about.
