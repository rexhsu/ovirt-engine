package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(MockitoJUnitRunner.class)
public class DiskValidatorTest {

    @Mock
    private VmDao vmDao;

    private DiskValidator validator;
    private DiskImage disk;
    private LunDisk lunDisk;
    private DiskValidator lunValidator;

    private static DiskImage createDiskImage() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        return disk;
    }

    private static LunDisk createLunDisk() {
        LunDisk disk = new LunDisk();
        LUNs lun = new LUNs();
        lun.setLUNId("lun_id");
        lun.setLunType(StorageType.ISCSI);
        disk.setLun(lun);
        return disk;
    }

    private static VM createVM() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        vm.setId(Guid.newGuid());
        vm.setVmOs(1);
        return vm;
    }

    @Before
    public void setUp() {
        disk = createDiskImage();
        disk.setDiskAlias("disk1");
        validator = spy(new DiskValidator(disk));
        doReturn(vmDao).when(validator).getVmDao();
    }

    private void setupForLun() {
        lunDisk = createLunDisk();
        lunValidator = spy(new DiskValidator(lunDisk));
        doReturn(vmDao).when(lunValidator).getVmDao();
    }

    private VmDevice createVmDeviceForDisk(VM vm, Disk disk, Guid snapshotId, boolean isPlugged) {
        VmDevice device = new VmDevice();
        device.setId(new VmDeviceId(vm.getId(), disk.getId()));
        device.setSnapshotId(snapshotId);
        device.setIsPlugged(isPlugged);
        return device;
    }

    public List<Pair<VM, VmDevice>> prepareForCheckingIfDiskPluggedToVmsThatAreNotDown() {
        VM vm1 = createVM();
        VM vm2 = createVM();
        VmDevice device1 = createVmDeviceForDisk(vm1, disk, null, true);
        VmDevice device2 = createVmDeviceForDisk(vm1, disk, null, true);
        List<Pair<VM, VmDevice>> vmsInfo = new LinkedList<>();
        vmsInfo.add(new Pair<>(vm1, device1));
        vmsInfo.add(new Pair<>(vm2, device2));
        when(vmDao.getVmsWithPlugInfo(disk.getId())).thenReturn(vmsInfo);
        return vmsInfo;
    }

    @Test
    public void diskPluggedToVmsThatAreNotDownValid() {
        List<Pair<VM, VmDevice>> vmsInfo = prepareForCheckingIfDiskPluggedToVmsThatAreNotDown();
        assertThat(validator.isDiskPluggedToVmsThatAreNotDown(false, vmsInfo), isValid());
    }

    @Test
    public void diskPluggedToVmsThatAreNotDownFail() {
        List<Pair<VM, VmDevice>> vmsInfo = prepareForCheckingIfDiskPluggedToVmsThatAreNotDown();
        vmsInfo.get(0).getFirst().setStatus(VMStatus.Up);
        assertThat(validator.isDiskPluggedToVmsThatAreNotDown(false, vmsInfo),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN));
    }

    @Test
    public void diskPluggedToVmsNotAsSnapshotSuccess() {
        List<Pair<VM, VmDevice>> vmsInfo = prepareForCheckingIfDiskPluggedToVmsThatAreNotDown();
        vmsInfo.get(0).getFirst().setStatus(VMStatus.Up);
        vmsInfo.get(1).getFirst().setStatus(VMStatus.Up);
        assertThat(validator.isDiskPluggedToVmsThatAreNotDown(true, vmsInfo),
                isValid());
    }

    @Test
    public void diskPluggedToVmsCheckSnapshotsFail() {
        List<Pair<VM, VmDevice>> vmsInfo = prepareForCheckingIfDiskPluggedToVmsThatAreNotDown();
        vmsInfo.get(1).getFirst().setStatus(VMStatus.Up);
        vmsInfo.get(1).getSecond().setSnapshotId(Guid.newGuid());
        assertThat(validator.isDiskPluggedToVmsThatAreNotDown(true, vmsInfo),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN));
    }

    @Test
    public void testIsUsingScsiReservationValidWhenSgioIsUnFiltered() {
        setupForLun();

        LunDisk lunDisk1 = createLunDisk(ScsiGenericIO.UNFILTERED);

        assertThat(lunValidator.isUsingScsiReservationValid(createVM(), createDiskVmElementUsingScsiReserevation(), lunDisk1),
                isValid());
    }

    @Test
    public void testIsUsingScsiReservationValidWhenSgioIsFiltered() {
        setupForLun();

        LunDisk lunDisk1 = createLunDisk(ScsiGenericIO.FILTERED);

        assertThat(lunValidator.isUsingScsiReservationValid(createVM(), createDiskVmElementUsingScsiReserevation(), lunDisk1),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_SGIO_IS_FILTERED));
    }

    @Test
    public void testDiskAttachedToVMValid() {
        VM vm = createVM();
        when(vmDao.getVmsListForDisk(any(Guid.class), anyBoolean())).thenReturn(Collections.singletonList(vm));
        assertThat(validator.isDiskAttachedToVm(vm), isValid());
    }

    @Test
    public void testDiskAttachedToVMFail() {
        VM vm = createVM();
        when(vmDao.getVmsListForDisk(any(Guid.class), anyBoolean())).thenReturn(Collections.emptyList());
        assertThat(validator.isDiskAttachedToVm(vm), failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_ATTACHED_TO_VM));
    }

    @Test
    public void testDiskAttachedToAnyVM() {
        when(vmDao.getVmsListForDisk(any(Guid.class), anyBoolean())).thenReturn(Collections.emptyList());
        assertThat(validator.isDiskAttachedToAnyVm(), isValid());
    }

    @Test
    public void testDiskAttachedToAnyVMFails() {
        VM vm1 = createVM();
        VM vm2 = createVM();
        vm1.setName("Vm1");
        vm2.setName("Vm2");
        List<VM> vmList = Arrays.asList(vm1, vm2);

        when(vmDao.getVmsListForDisk(any(Guid.class), anyBoolean())).thenReturn(vmList);
        String[] expectedReplacements = {
                ReplacementUtils.createSetVariableString(DiskValidator.DISK_NAME_VARIABLE, disk.getDiskAlias()),
                ReplacementUtils.createSetVariableString(DiskValidator.VM_LIST, "Vm1,Vm2")};

        assertThat(validator.isDiskAttachedToAnyVm(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_ATTACHED_TO_VMS, expectedReplacements));
    }

    @Test
    public void testDiskAttachedToVMFailWithCorrectReplacements() {
        VM vm = createVM();
        vm.setName("MyVm");
        disk.setDiskAlias("MyDisk");
        when(vmDao.getVmsListForDisk(any(Guid.class), anyBoolean())).thenReturn(Collections.emptyList());
        String[] expectedReplacements = {
                ReplacementUtils.createSetVariableString(DiskValidator.DISK_NAME_VARIABLE, disk.getDiskAlias()),
                ReplacementUtils.createSetVariableString(DiskValidator.VM_NAME_VARIABLE, vm.getName())};
        assertThat(validator.isDiskAttachedToVm(vm), failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_ATTACHED_TO_VM, expectedReplacements));
    }

    private LunDisk createLunDisk(ScsiGenericIO sgio) {
        LunDisk lunDisk = createLunDisk();
        lunDisk.setSgio(sgio);

        return lunDisk;
    }

    private static DiskVmElement createDiskVmElementUsingScsiReserevation() {
        DiskVmElement dve = new DiskVmElement();
        dve.setUsingScsiReservation(true);
        return dve;
    }


}
