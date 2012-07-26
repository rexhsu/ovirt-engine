package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class NewPoolModelBehavior extends PoolModelBehaviorBase {

    @Override
    protected void setupSelectedTemplate(ListModel model, List<VmTemplate> templates) {
        getModel().getTemplate().setSelectedItem(Linq.<VmTemplate> FirstOrDefault(templates));
    }

    public void Template_SelectedItemChanged() {
        VmBase vmBase = (VmTemplate) getModel().getTemplate().getSelectedItem();
        setupWindowModelFrom(vmBase);
    }

    @Override
    protected DisplayType extractDisplayType(VmBase vmBase) {
        if (vmBase instanceof VmTemplate) {
            return ((VmTemplate) vmBase).getdefault_display_type();
        }

        return null;
    }
}
