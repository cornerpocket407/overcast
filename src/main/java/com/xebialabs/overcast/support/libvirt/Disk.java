/**
 *    Copyright 2012-2016 XebiaLabs B.V.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xebialabs.overcast.support.libvirt;

import java.io.IOException;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.StorageVolInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.MoreObjects;

import com.xebialabs.overcast.support.libvirt.jdom.DiskXml;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

public class Disk {
    private static final Logger log = LoggerFactory.getLogger(Disk.class);

    public String device;
    public String file;
    public String format;
    private StorageVol volume;

    public Disk(String device, String file, StorageVol volume, String format) {
        checkNotNull(emptyToNull(device));
        checkNotNull(emptyToNull(file));
        checkNotNull(volume);
        checkNotNull(emptyToNull(format));

        this.device = device;
        this.file = file;
        this.volume = volume;
        this.format = format;
    }

    public StorageVolInfo getInfo() {
        try {
            return volume.getInfo();
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    public String getName() {
        try {
            return volume.getName();
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    public String getBaseName() {
        String name = getName();
        int idx = name.lastIndexOf('.');
        if (idx == -1) {
            return name;
        }
        return name.substring(0, idx);
    }

    public StorageVol getVolume() {
        return volume;
    }

    public StoragePool getStoragePool() {
        try {
            return volume.storagePoolLookupByVolume();
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    public StorageVol createCloneWithBackingStore(String name) {
        try {
            String volumeXml = DiskXml.cloneVolumeXml(this, name);
            log.debug("Creating volume with xml={}", volumeXml);
            StorageVol vol = getStoragePool().storageVolCreateXML(volumeXml, 0);
            return vol;
        } catch (LibvirtException e) {
            throw new LibvirtRuntimeException(e);
        } catch (IOException e) {
            throw new LibvirtRuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", getName())
                .add("format", format)
                .add("device", device)
                .add("file", file).toString();
    }
}
