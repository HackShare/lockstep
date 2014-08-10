package com.picostuff.lockstep;

/**
 * There's one of these per thread. It manages the transfer of workspace items between workspaces to keep them in sync.
 * 
 * TODO: on startup, we are basically recovering to what we last know about server files, which means that we have
 * to persist that knowledge somewhere.  Basically, if we don't recover the state,
 * we are forced to wipe out all local changes.
 * @author chenglim
 *
 */
public class Worker {

}
