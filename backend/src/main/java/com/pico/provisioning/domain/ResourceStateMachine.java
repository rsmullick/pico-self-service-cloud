package com.pico.provisioning.domain;

public final class ResourceStateMachine {

    public ResourceStatus transition(
            ResourceStatus current,
            ResourceAction action
    ) {

        return switch (current) {

            case PENDING -> switch (action) {

                case START_PROVISIONING ->
                        ResourceStatus.PROVISIONING;

                default ->
                        throw invalid(current, action);
            };

            case PROVISIONING -> switch (action) {

                case PROVISION_SUCCESS ->
                        ResourceStatus.RUNNING;

                case PROVISION_FAILED ->
                        ResourceStatus.FAILED;

                default ->
                        throw invalid(current, action);
            };

            case RUNNING -> switch (action) {

                case STOP ->
                        ResourceStatus.STOPPED;

                case TERMINATE ->
                        ResourceStatus.TERMINATED;

                default ->
                        throw invalid(current, action);
            };

            case STOPPED -> switch (action) {

                case START ->
                        ResourceStatus.RUNNING;

                case TERMINATE ->
                        ResourceStatus.TERMINATED;

                default ->
                        throw invalid(current, action);
            };

            case FAILED, TERMINATED ->
                    throw invalid(current, action);
        };
    }

    private IllegalStateException invalid(
            ResourceStatus status,
            ResourceAction action
    ) {

        return new IllegalStateException(
                "Invalid transition: "
                        + status
                        + " -> "
                        + action
        );
    }
}
