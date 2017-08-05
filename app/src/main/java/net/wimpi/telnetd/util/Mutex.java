//@license@
package net.wimpi.telnetd.util;

import android.util.Log;

/**
 * A simple non-reentrant mutual exclusion lock.
 * The lock is free upon construction. Each acquire gets the
 * lock, and each release frees it. Releasing a lock that
 * is already free has no effect.
 * <p/>
 * This implementation makes no attempt to provide any fairness
 * or ordering guarantees. If you need them, consider using one of
 * the Semaphore implementations as a locking mechanism.
 * <p/>
 * <b>Sample usage</b><br>
 * <p/>
 * Mutex can be useful in constructions that cannot be
 * expressed using java synchronized blocks because the
 * acquire/release pairs do not occur in the same method or
 * code block. For example, you can use them for hand-over-hand
 * locking across the nodes of a linked list. This allows
 * extremely fine-grained locking,  and so increases
 * potential concurrency, at the cost of additional complexity and
 * overhead that would normally make this worthwhile only in cases of
 * extreme contention.
 * <pre>
 * class Node {
 *   Object item;
 *   Node next;
 *   Mutex lock = new Mutex(); // each node keeps its own lock
 * <p/>
 *   Node(Object x, Node n) { item = x; next = n; }
 * }
 * <p/>
 * class List {
 *    protected Node head; // pointer to first node of list
 * <p/>
 *    // Use plain java synchronization to protect head field.
 *    //  (We could instead use a Mutex here too but there is no
 *    //  reason to do so.)
 *    protected synchronized Node getHead() { return head; }
 * <p/>
 *    boolean search(Object x) throws InterruptedException {
 *      Node p = getHead();
 *      if (p == null) return false;
 * <p/>
 *      //  (This could be made more compact, but for clarity of illustration,
 *      //  all of the cases that can arise are handled separately.)
 * <p/>
 *      p.lock.acquire();              // Prime loop by acquiring first lock.
 *                                     //    (If the acquire fails due to
 *                                     //    interrupt, the method will throw
 *                                     //    InterruptedException now,
 *                                     //    so there is no need for any
 *                                     //    further cleanup.)
 *      for (;;) {
 *        if (x.equals(p.item)) {
 *          p.lock.release();          // release current before return
 *          return true;
 *        }
 *        else {
 *          Node nextp = p.next;
 *          if (nextp == null) {
 *            p.lock.release();       // release final lock that was held
 *            return false;
 *          }
 *          else {
 *            try {
 *              nextp.lock.acquire(); // get next lock before releasing current
 *            }
 *            catch (InterruptedException ex) {
 *              p.lock.release();    // also release current if acquire fails
 *              throw ex;
 *            }
 *            p.lock.release();      // release old lock now that new one held
 *            p = nextp;
 *          }
 *        }
 *      }
 *    }
 * <p/>
 *    synchronized void add(Object x) { // simple prepend
 *      // The use of `synchronized'  here protects only head field.
 *      // The method does not need to wait out other traversers
 *      // who have already made it past head.
 * <p/>
 *      head = new Node(x, head);
 *    }
 * <p/>
 *    // ...  other similar traversal and update methods ...
 * }
 *
 * @author Doug Lea
 * @version @version@ (@date@)
 */
public class Mutex {

  /**
   * The lock status *
   */
  protected boolean inuse_ = false;

  public void acquire() throws InterruptedException {
    //Log.d(TAG, "acquire()::" + Thread.currentThread().toString());
    if (Thread.interrupted()) throw new InterruptedException();
    synchronized (this) {
      try {
        while (inuse_) wait();
        inuse_ = true;
      } catch (InterruptedException ex) {
        notify();
        throw ex;
      }
    }
  }//accquire

  public synchronized void release() {
    //Log.d(TAG, "release()::" + Thread.currentThread().toString());
    inuse_ = false;
    notify();
  }//release


  public boolean attempt(long msecs) throws InterruptedException {
    //Log.d(TAG, "attempt()::" + Thread.currentThread().toString());
    if (Thread.interrupted()) throw new InterruptedException();
    synchronized (this) {
      if (!inuse_) {
        inuse_ = true;
        return true;
      } else if (msecs <= 0)
        return false;
      else {
        long waitTime = msecs;
        long start = System.currentTimeMillis();
        try {
          for (; ;) {
            wait(waitTime);
            if (!inuse_) {
              inuse_ = true;
              return true;
            } else {
              waitTime = msecs - (System.currentTimeMillis() - start);
              if (waitTime <= 0)
                return false;
            }
          }
        } catch (InterruptedException ex) {
          notify();
          throw ex;
        }
      }
    }
  }//attempt

}//class Mutex