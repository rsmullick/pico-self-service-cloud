package com.pico.provisioning.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResourceStateMachineTest {

    private final ResourceStateMachine fsm = new ResourceStateMachine();

    // Valid transitions
    @Test
    public void pending_to_provisioning() {
        ResourceStatus result = fsm.transition(ResourceStatus.PENDING, ResourceAction.START_PROVISIONING);
        assertEquals(ResourceStatus.PROVISIONING, result);
    }

    @Test
    public void provisioning_to_running() {
        ResourceStatus result = fsm.transition(ResourceStatus.PROVISIONING, ResourceAction.PROVISION_SUCCESS);
        assertEquals(ResourceStatus.RUNNING, result);
    }

    @Test
    public void provisioning_to_failed() {
        ResourceStatus result = fsm.transition(ResourceStatus.PROVISIONING, ResourceAction.PROVISION_FAILED);
        assertEquals(ResourceStatus.FAILED, result);
    }

    @Test
    public void running_to_stopped() {
        ResourceStatus result = fsm.transition(ResourceStatus.RUNNING, ResourceAction.STOP);
        assertEquals(ResourceStatus.STOPPED, result);
    }

    @Test
    public void stopped_to_running() {
        ResourceStatus result = fsm.transition(ResourceStatus.STOPPED, ResourceAction.START);
        assertEquals(ResourceStatus.RUNNING, result);
    }

    @Test
    public void running_to_terminated() {
        ResourceStatus result = fsm.transition(ResourceStatus.RUNNING, ResourceAction.TERMINATE);
        assertEquals(ResourceStatus.TERMINATED, result);
    }

    @Test
    public void stopped_to_terminated() {
        ResourceStatus result = fsm.transition(ResourceStatus.STOPPED, ResourceAction.TERMINATE);
        assertEquals(ResourceStatus.TERMINATED, result);
    }

    // Invalid transitions
    @Test
    public void pending_to_terminate_isInvalid() {
        assertThrows(IllegalStateException.class, () ->
                fsm.transition(ResourceStatus.PENDING, ResourceAction.TERMINATE)
        );
    }

    @Test
    public void running_to_provisionSuccess_isInvalid() {
        assertThrows(IllegalStateException.class, () ->
                fsm.transition(ResourceStatus.RUNNING, ResourceAction.PROVISION_SUCCESS)
        );
    }

    @Test
    public void failed_to_start_isInvalid() {
        assertThrows(IllegalStateException.class, () ->
                fsm.transition(ResourceStatus.FAILED, ResourceAction.START)
        );
    }
}
