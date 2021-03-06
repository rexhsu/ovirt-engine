package org.ovirt.engine.core.bll.scheduling.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class ClusterPolicyCRUDCommandTest extends BaseCommandTest {
    @Test
    public void testCheckAddEditValidations() {
        Guid clusterPolicyId = new Guid("e754440b-76a6-4099-8235-4565ab4b5521");
        ClusterPolicy clusterPolicy = new ClusterPolicy();
        clusterPolicy.setId(clusterPolicyId);
        ClusterPolicyCRUDCommand command =
                new ClusterPolicyCRUDCommand(new ClusterPolicyCRUDParameters(clusterPolicyId,
                        clusterPolicy), null) {

                    @Override
                    protected void executeCommand() {
                    }
                };
        command.schedulingManager = mockScheduler();
        assertTrue(command.checkAddEditValidations());
    }

    @Test
    public void testCheckAddEditValidationsFailOnParameters() {
        Guid clusterPolicyId = new Guid("e754440b-76a6-4099-8235-4565ab4b5521");
        ClusterPolicy clusterPolicy = new ClusterPolicy();
        clusterPolicy.setId(clusterPolicyId);
        HashMap<String, String> parameterMap = new HashMap<>();
        parameterMap.put("fail?", "sure, fail!");
        clusterPolicy.setParameterMap(parameterMap);
        ClusterPolicyCRUDCommand command =
                new ClusterPolicyCRUDCommand(new ClusterPolicyCRUDParameters(clusterPolicyId,
                        clusterPolicy), null) {

                    @Override
                    protected void executeCommand() {
                    }
                };
        command.schedulingManager = mockScheduler();
        assertFalse(command.checkAddEditValidations());
    }

    private SchedulingManager mockScheduler() {
        SchedulingManager mock = mock(SchedulingManager.class);
        when(mock.getClusterPolicies()).thenReturn(Collections.emptyList());
        return mock;
    }

}
