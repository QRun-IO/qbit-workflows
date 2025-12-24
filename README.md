# qbit-workflows

State machine workflows for QQQ applications.

**For:** QQQ developers building approval flows, order pipelines, or any multi-step business processes  
**Status:** Stable

## Why This Exists

Business processes have states. Orders go from pending to approved to shipped. Support tickets move through triage, investigation, and resolution. Building these flows means tracking state, validating transitions, and triggering actions at each step.

This QBit provides a declarative workflow engine. Define states and transitions, and the system enforces valid paths, runs actions on transitions, and tracks history automatically.

## Features

- **State Definitions** - Define allowed states for any entity
- **Transition Rules** - Control which states can move to which other states
- **Transition Actions** - Execute code when moving between states
- **Role-Based Transitions** - Restrict who can trigger which transitions
- **Transition History** - Automatic audit log of all state changes
- **Dashboard Integration** - Status badges and transition buttons in the UI

## Quick Start

### Prerequisites

- QQQ application (v0.20+)
- Database backend configured

### Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>io.qrun</groupId>
    <artifactId>qbit-workflows</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Register the QBit

```java
public class AppMetaProvider extends QMetaProvider {
    @Override
    public void configure(QInstance qInstance) {
        new WorkflowsQBit().configure(qInstance);
    }
}
```

### Define a Workflow

```java
new QWorkflowMetaData()
    .withName("orderWorkflow")
    .withTable("order")
    .withStatusField("status")
    .withStates(
        new QWorkflowState("pending", "Pending"),
        new QWorkflowState("approved", "Approved"),
        new QWorkflowState("shipped", "Shipped"),
        new QWorkflowState("cancelled", "Cancelled")
    )
    .withTransitions(
        new QWorkflowTransition("pending", "approved"),
        new QWorkflowTransition("pending", "cancelled"),
        new QWorkflowTransition("approved", "shipped"),
        new QWorkflowTransition("approved", "cancelled")
    );
```

## Usage

### Transition Actions

```java
new QWorkflowTransition("approved", "shipped")
    .withAction(new ShipOrderAction());

public class ShipOrderAction implements WorkflowTransitionAction {
    @Override
    public void execute(WorkflowTransitionContext context) {
        // Send shipping notification
        // Update inventory
        // Generate tracking number
    }
}
```

### Role-Based Transitions

```java
// Only managers can approve
new QWorkflowTransition("pending", "approved")
    .withAllowedRoles("manager", "admin");
```

### Transition Validation

```java
new QWorkflowTransition("approved", "shipped")
    .withValidator((context) -> {
        if (context.getRecord().getValue("shippingAddress") == null) {
            throw new WorkflowException("Shipping address required");
        }
    });
```

### Querying by State

```java
// Find all pending orders
QueryInput query = new QueryInput()
    .withTableName("order")
    .withFilter(new QQueryFilter()
        .withCriteria("status", Operator.EQUALS, "pending"));
```

## Configuration

The QBit creates a transition history table automatically:

| Table | Purpose |
|-------|---------|
| `workflow_transition_log` | Audit trail of all state changes |

Each log entry records: record ID, from state, to state, user, timestamp, and optional comments.

## Project Status

Stable and production-ready.

### Roadmap

- Parallel state support (multiple active states)
- Scheduled transitions (auto-expire after N days)
- Workflow visualization in dashboard

## Contributing

1. Fork the repository
2. Create a feature branch
3. Run tests: `mvn clean verify`
4. Submit a pull request

## License

Proprietary - QRun.IO
