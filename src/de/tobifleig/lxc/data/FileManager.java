/*
 * Copyright 2009, 2010, 2011, 2012 Tobias Fleig (tobifleig gmail com)
 *
 * All rights reserved.
 *
 * This file is part of LanXchange.
 *
 * LanXchange is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LanXchange is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LanXchange. If not, see <http://www.gnu.org/licenses/>.
 */
package de.tobifleig.lxc.data;

import de.tobifleig.lxc.net.LXCInstance;
import de.tobifleig.lxc.net.TransFileList;
import java.util.*;

/**
 * Manages own & remote LXCFiles
 *
 * @author Tobias Fleig <tobifleig googlemail com>
 */
public class FileManager {

    /**
     * Contains all LXCFiles.
     */
    private LinkedList<LXCFile> files;

    /**
     * Creates a new FileManager.
     */
    public FileManager() {
	files = new LinkedList<LXCFile>();
    }

    /**
     * Computes incoming TransFileLists.
     *
     * @param receivedList the received list
     * @param sender the origin of this list
     */
    public void computeFileList(TransFileList receivedList, LXCInstance sender) {
	List<LXCFile> rawList = receivedList.getAll();
	Iterator<LXCFile> iter = files.iterator();
	// Remove all files no longer offerd by this instance
	while (iter.hasNext()) {
	    LXCFile file = iter.next();
	    if (sender.equals(file.getInstance())) {
		if (!rawList.contains(file)) {
		    // no longer offered, remove it, if not download{ing,ed}
		    if (file.getJobs().isEmpty() && !file.isAvailable()) {
			iter.remove();
		    } else {
			// keep, ignore in next step:
			rawList.remove(file);
		    }
		} else {
		    // already known, ignore in next step
		    rawList.remove(file);
		}
	    }
	}

	receivedList.setInstance(sender);
	receivedList.limitTransVersions();
	files.addAll(rawList);
    }

    /**
     * Removes all LXCFiles offered by the given LXCInstance
     *
     * @param instance the instance which files should be no longer available
     */
    public void removeFromInstance(LXCInstance instance) {
	Iterator<LXCFile> iter = files.iterator();
	while (iter.hasNext()) {
	    LXCFile file = iter.next();
	    if (file.getInstance().equals(instance)) {
		// only delete if not download{ing,ed}
		if (file.getJobs().isEmpty() && !file.isAvailable()) {
		    iter.remove();
		}
	    }
	}
    }

    /**
     * Returns the local representation for a LXCFile sent by a remote instance.
     * This step is required to upload a file, because only the local representation contains the paths to the source files.
     *
     * @param remoteRepresentation the remote representation
     * @return the local representation, or null if file not available
     */
    public LXCFile localRepresentation(LXCFile remoteRepresentation) {
	int index = files.indexOf(remoteRepresentation);
	if (index != -1) {
	    return files.get(index);
	}
	return null;
    }

    /**
     * Creates an up-to-date TransFileList containing all LXCFiles offered by this instance.
     *
     * @return a TransFileList containing all LXCFiles
     */
    public TransFileList getTransFileList() {
	ArrayList<LXCFile> offeredFiles = new ArrayList<LXCFile>();
	for (LXCFile file : files) {
	    if (file.isLocal()) {
		offeredFiles.add(file);
	    }
	}
	return new TransFileList(offeredFiles);
    }

    /**
     * Whether there are any files.
     * Like !isEmpty()
     *
     * @return true, if at least element is managed
     */
    public boolean containsElements() {
	return !files.isEmpty();
    }

    /**
     * Returns a list of all known Files.
     * This list is backed by the internal list, but not modifiable.
     *
     * @see java.util.Collections
     * @return a list of all known files, backed but unmodifiable
     */
    public List<LXCFile> getList() {
	return Collections.unmodifiableList(files);
    }

    /**
     * Adds a file offered by the local instance.
     *
     * @param newfile the new file
     */
    public void addLocal(LXCFile newfile) {
	// do not allow duplicates
	for (LXCFile file : files) {
	    if (file.equals(newfile)) {
		return;
	    }
	}
	files.add(newfile);
    }

    /**
     * Removes a file offered by the local instance.
     *
     * @param file a file offered by the local instance
     */
    public void removeLocal(LXCFile file) {
	files.remove(file);
    }

    /**
     * Returns true, if there are any transfers running.
     *
     * @return true, if still transferring
     */
    public boolean transferRunning() {
	for (LXCFile file : files) {
	    if (!file.getJobs().isEmpty()) {
		return true;
	    }
	}
	return false;
    }
}
