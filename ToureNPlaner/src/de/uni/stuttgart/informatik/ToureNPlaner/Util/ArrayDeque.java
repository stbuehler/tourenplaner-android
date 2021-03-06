/*
 * Written by Josh Bloch of Google Inc. and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain.
 */

package de.uni.stuttgart.informatik.ToureNPlaner.Util;

// BEGIN android-note
// removed link to collections framework docs
// END android-note

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Resizable-array implementation of the {@link Deque} interface.  Array
 * deques have no capacity restrictions; they grow as necessary to support
 * usage.  They are not thread-safe; in the absence of external
 * synchronization, they do not support concurrent access by multiple threads.
 * Null elements are prohibited.  This class is likely to be faster than
 * {@link java.util.Stack} when used as a stack, and faster than {@link java.util.LinkedList}
 * when used as a queue.
 * <p/>
 * <p>Most <tt>ArrayDeque</tt> operations run in amortized constant time.
 * Exceptions include {@link #remove(Object) remove}, {@link
 * #removeFirstOccurrence removeFirstOccurrence}, {@link #removeLastOccurrence
 * removeLastOccurrence}, {@link #contains contains}, {@link #iterator
 * iterator.remove()}, and the bulk operations, all of which run in linear
 * time.
 * <p/>
 * <p>The iterators returned by this class's <tt>iterator</tt> method are
 * <i>fail-fast</i>: If the deque is modified at any time after the iterator
 * is created, in any way except through the iterator's own <tt>remove</tt>
 * method, the iterator will generally throw a {@link
 * java.util.ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 * <p/>
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 * <p/>
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link java.util.Collection} and {@link
 * java.util.Iterator} interfaces.
 *
 * @param <E> the type of elements held in this collection
 * @author Josh Bloch and Doug Lea
 * @since 1.6
 */
@SuppressWarnings("unchecked")
public class ArrayDeque<E> extends AbstractCollection<E>
		implements Deque<E>, Cloneable, Serializable {
	/**
	 * The array in which the elements of the deque are stored.
	 * The capacity of the deque is the length of this array, which is
	 * always a power of two. The array is never allowed to become
	 * full, except transiently within an addX method where it is
	 * resized (see doubleCapacity) immediately upon becoming full,
	 * thus avoiding head and tail wrapping around to equal each
	 * other.  We also guarantee that all array cells not holding
	 * deque elements are always null.
	 */
	private transient E[] elements;

	/**
	 * The index of the element at the head of the deque (which is the
	 * element that would be removed by remove() or pop()); or an
	 * arbitrary number equal to tail if the deque is empty.
	 */
	private transient int head;

	/**
	 * The index at which the next element would be added to the tail
	 * of the deque (via addLast(E), add(E), or push(E)).
	 */
	private transient int tail;

	/**
	 * The minimum capacity that we'll use for a newly created deque.
	 * Must be a power of 2.
	 */
	// private static final int MIN_INITIAL_CAPACITY = 8;

	// ******  Array allocation and resizing utilities ******

	/**
	 * Double the capacity of this deque.  Call only when full, i.e.,
	 * when head and tail have wrapped around to become equal.
	 */
	private void doubleCapacity() {
		assert head == tail;
		int p = head;
		int n = elements.length;
		int r = n - p; // number of elements to the right of p
		int newCapacity = n << 1;
		if (newCapacity < 0)
			throw new IllegalStateException("Sorry, deque too big");
		Object[] a = new Object[newCapacity];
		System.arraycopy(elements, p, a, 0, r);
		System.arraycopy(elements, 0, a, r, p);
		elements = (E[]) a;
		head = 0;
		tail = n;
	}

	/**
	 * Copies the elements from our element array into the specified array,
	 * in order (from first to last element in the deque).  It is assumed
	 * that the array is large enough to hold all elements in the deque.
	 *
	 * @return its argument
	 */
	private <T> T[] copyElements(T[] a) {
		if (head < tail) {
			System.arraycopy(elements, head, a, 0, size());
		} else if (head > tail) {
			int headPortionLen = elements.length - head;
			System.arraycopy(elements, head, a, 0, headPortionLen);
			System.arraycopy(elements, 0, a, headPortionLen, tail);
		}
		return a;
	}

	/**
	 * Constructs an empty array deque with an initial capacity
	 * sufficient to hold 16 elements.
	 */
	public ArrayDeque() {
		elements = (E[]) new Object[16];
	}

	// The main insertion and extraction methods are addFirst,
	// addLast, pollFirst, pollLast. The other methods are defined in
	// terms of these.

	/**
	 * Inserts the specified element at the front of this deque.
	 *
	 * @param e the element to add
	 * @throws NullPointerException if the specified element is null
	 */
	public void addFirst(E e) {
		if (e == null)
			throw new NullPointerException();
		elements[head = (head - 1) & (elements.length - 1)] = e;
		if (head == tail)
			doubleCapacity();
	}

	/**
	 * Inserts the specified element at the end of this deque.
	 * <p/>
	 * <p>This method is equivalent to {@link #add}.
	 *
	 * @param e the element to add
	 * @throws NullPointerException if the specified element is null
	 */
	public void addLast(E e) {
		if (e == null)
			throw new NullPointerException();
		elements[tail] = e;
		if ((tail = (tail + 1) & (elements.length - 1)) == head)
			doubleCapacity();
	}

	/**
	 * Inserts the specified element at the front of this deque.
	 *
	 * @param e the element to add
	 * @return <tt>true</tt> (as specified by {@link Deque#offerFirst})
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean offerFirst(E e) {
		addFirst(e);
		return true;
	}

	/**
	 * Inserts the specified element at the end of this deque.
	 *
	 * @param e the element to add
	 * @return <tt>true</tt> (as specified by {@link Deque#offerLast})
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean offerLast(E e) {
		addLast(e);
		return true;
	}

	/**
	 * @throws java.util.NoSuchElementException
	 *          {@inheritDoc}
	 */
	public E removeFirst() {
		E x = pollFirst();
		if (x == null)
			throw new NoSuchElementException();
		return x;
	}

	/**
	 * @throws java.util.NoSuchElementException
	 *          {@inheritDoc}
	 */
	public E removeLast() {
		E x = pollLast();
		if (x == null)
			throw new NoSuchElementException();
		return x;
	}

	public E pollFirst() {
		int h = head;
		E result = elements[h]; // Element is null if deque empty
		if (result == null)
			return null;
		elements[h] = null;     // Must null out slot
		head = (h + 1) & (elements.length - 1);
		return result;
	}

	public E pollLast() {
		int t = (tail - 1) & (elements.length - 1);
		E result = elements[t];
		if (result == null)
			return null;
		elements[t] = null;
		tail = t;
		return result;
	}

	/**
	 * @throws java.util.NoSuchElementException
	 *          {@inheritDoc}
	 */
	public E getFirst() {
		E x = elements[head];
		if (x == null)
			throw new NoSuchElementException();
		return x;
	}

	/**
	 * @throws java.util.NoSuchElementException
	 *          {@inheritDoc}
	 */
	public E getLast() {
		E x = elements[(tail - 1) & (elements.length - 1)];
		if (x == null)
			throw new NoSuchElementException();
		return x;
	}

	public E peekFirst() {
		return elements[head]; // elements[head] is null if deque empty
	}

	public E peekLast() {
		return elements[(tail - 1) & (elements.length - 1)];
	}

	/**
	 * Removes the first occurrence of the specified element in this
	 * deque (when traversing the deque from head to tail).
	 * If the deque does not contain the element, it is unchanged.
	 * More formally, removes the first element <tt>e</tt> such that
	 * <tt>o.equals(e)</tt> (if such an element exists).
	 * Returns <tt>true</tt> if this deque contained the specified element
	 * (or equivalently, if this deque changed as a result of the call).
	 *
	 * @param o element to be removed from this deque, if present
	 * @return <tt>true</tt> if the deque contained the specified element
	 */
	public boolean removeFirstOccurrence(Object o) {
		if (o == null)
			return false;
		int mask = elements.length - 1;
		int i = head;
		E x;
		while ((x = elements[i]) != null) {
			if (o.equals(x)) {
				delete(i);
				return true;
			}
			i = (i + 1) & mask;
		}
		return false;
	}

	/**
	 * Removes the last occurrence of the specified element in this
	 * deque (when traversing the deque from head to tail).
	 * If the deque does not contain the element, it is unchanged.
	 * More formally, removes the last element <tt>e</tt> such that
	 * <tt>o.equals(e)</tt> (if such an element exists).
	 * Returns <tt>true</tt> if this deque contained the specified element
	 * (or equivalently, if this deque changed as a result of the call).
	 *
	 * @param o element to be removed from this deque, if present
	 * @return <tt>true</tt> if the deque contained the specified element
	 */
	public boolean removeLastOccurrence(Object o) {
		if (o == null)
			return false;
		int mask = elements.length - 1;
		int i = (tail - 1) & mask;
		E x;
		while ((x = elements[i]) != null) {
			if (o.equals(x)) {
				delete(i);
				return true;
			}
			i = (i - 1) & mask;
		}
		return false;
	}

	// *** Queue methods ***

	/**
	 * Inserts the specified element at the end of this deque.
	 * <p/>
	 * <p>This method is equivalent to {@link #addLast}.
	 *
	 * @param e the element to add
	 * @return <tt>true</tt> (as specified by {@link java.util.Collection#add})
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean add(E e) {
		addLast(e);
		return true;
	}

	/**
	 * Inserts the specified element at the end of this deque.
	 * <p/>
	 * <p>This method is equivalent to {@link #offerLast}.
	 *
	 * @param e the element to add
	 * @return <tt>true</tt> (as specified by {@link java.util.Queue#offer})
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean offer(E e) {
		return offerLast(e);
	}

	/**
	 * Retrieves and removes the head of the queue represented by this deque.
	 * <p/>
	 * This method differs from {@link #poll poll} only in that it throws an
	 * exception if this deque is empty.
	 * <p/>
	 * <p>This method is equivalent to {@link #removeFirst}.
	 *
	 * @return the head of the queue represented by this deque
	 * @throws java.util.NoSuchElementException
	 *          {@inheritDoc}
	 */
	public E remove() {
		return removeFirst();
	}

	/**
	 * Retrieves and removes the head of the queue represented by this deque
	 * (in other words, the first element of this deque), or returns
	 * <tt>null</tt> if this deque is empty.
	 * <p/>
	 * <p>This method is equivalent to {@link #pollFirst}.
	 *
	 * @return the head of the queue represented by this deque, or
	 *         <tt>null</tt> if this deque is empty
	 */
	public E poll() {
		return pollFirst();
	}

	/**
	 * Retrieves, but does not remove, the head of the queue represented by
	 * this deque.  This method differs from {@link #peek peek} only in
	 * that it throws an exception if this deque is empty.
	 * <p/>
	 * <p>This method is equivalent to {@link #getFirst}.
	 *
	 * @return the head of the queue represented by this deque
	 * @throws java.util.NoSuchElementException
	 *          {@inheritDoc}
	 */
	public E element() {
		return getFirst();
	}

	/**
	 * Retrieves, but does not remove, the head of the queue represented by
	 * this deque, or returns <tt>null</tt> if this deque is empty.
	 * <p/>
	 * <p>This method is equivalent to {@link #peekFirst}.
	 *
	 * @return the head of the queue represented by this deque, or
	 *         <tt>null</tt> if this deque is empty
	 */
	public E peek() {
		return peekFirst();
	}

	// *** Stack methods ***

	/**
	 * Pushes an element onto the stack represented by this deque.  In other
	 * words, inserts the element at the front of this deque.
	 * <p/>
	 * <p>This method is equivalent to {@link #addFirst}.
	 *
	 * @param e the element to push
	 * @throws NullPointerException if the specified element is null
	 */
	public void push(E e) {
		addFirst(e);
	}

	/**
	 * Pops an element from the stack represented by this deque.  In other
	 * words, removes and returns the first element of this deque.
	 * <p/>
	 * <p>This method is equivalent to {@link #removeFirst()}.
	 *
	 * @return the element at the front of this deque (which is the top
	 *         of the stack represented by this deque)
	 * @throws java.util.NoSuchElementException
	 *          {@inheritDoc}
	 */
	public E pop() {
		return removeFirst();
	}

	private void checkInvariants() {
		assert elements[tail] == null;
		assert head == tail ? elements[head] == null :
				(elements[head] != null &&
						elements[(tail - 1) & (elements.length - 1)] != null);
		assert elements[(head - 1) & (elements.length - 1)] == null;
	}

	/**
	 * Removes the element at the specified position in the elements array,
	 * adjusting head and tail as necessary.  This can result in motion of
	 * elements backwards or forwards in the array.
	 * <p/>
	 * <p>This method is called delete rather than remove to emphasize
	 * that its semantics differ from those of {@link java.util.List#remove(int)}.
	 *
	 * @return true if elements moved backwards
	 */
	private boolean delete(int i) {
		checkInvariants();
		final E[] elements = this.elements;
		final int mask = elements.length - 1;
		final int h = head;
		final int t = tail;
		final int front = (i - h) & mask;
		final int back = (t - i) & mask;

		// Invariant: head <= i < tail mod circularity
		if (front >= ((t - h) & mask))
			throw new ConcurrentModificationException();

		// Optimize for least element motion
		if (front < back) {
			if (h <= i) {
				System.arraycopy(elements, h, elements, h + 1, front);
			} else { // Wrap around
				System.arraycopy(elements, 0, elements, 1, i);
				elements[0] = elements[mask];
				System.arraycopy(elements, h, elements, h + 1, mask - h);
			}
			elements[h] = null;
			head = (h + 1) & mask;
			return false;
		} else {
			if (i < t) { // Copy the null tail as well
				System.arraycopy(elements, i + 1, elements, i, back);
				tail = t - 1;
			} else { // Wrap around
				System.arraycopy(elements, i + 1, elements, i, mask - i);
				elements[mask] = elements[0];
				System.arraycopy(elements, 1, elements, 0, t);
				tail = (t - 1) & mask;
			}
			return true;
		}
	}

	// *** Collection Methods ***

	/**
	 * Returns the number of elements in this deque.
	 *
	 * @return the number of elements in this deque
	 */
	public int size() {
		return (tail - head) & (elements.length - 1);
	}

	/**
	 * Returns <tt>true</tt> if this deque contains no elements.
	 *
	 * @return <tt>true</tt> if this deque contains no elements
	 */
	public boolean isEmpty() {
		return head == tail;
	}

	/**
	 * Returns an iterator over the elements in this deque.  The elements
	 * will be ordered from first (head) to last (tail).  This is the same
	 * order that elements would be dequeued (via successive calls to
	 * {@link #remove} or popped (via successive calls to {@link #pop}).
	 *
	 * @return an iterator over the elements in this deque
	 */
	public Iterator<E> iterator() {
		return new DeqIterator();
	}

	private class DeqIterator implements Iterator<E> {
		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		private int cursor = head;

		/**
		 * Tail recorded at construction (also in remove), to stop
		 * iterator and also to check for comodification.
		 */
		private int fence = tail;

		/**
		 * Index of element returned by most recent call to next.
		 * Reset to -1 if element is deleted by a call to remove.
		 */
		private int lastRet = -1;

		public boolean hasNext() {
			return cursor != fence;
		}

		public E next() {
			if (cursor == fence)
				throw new NoSuchElementException();
			E result = elements[cursor];
			// This check doesn't catch all possible comodifications,
			// but does catch the ones that corrupt traversal
			if (tail != fence || result == null)
				throw new ConcurrentModificationException();
			lastRet = cursor;
			cursor = (cursor + 1) & (elements.length - 1);
			return result;
		}

		public void remove() {
			if (lastRet < 0)
				throw new IllegalStateException();
			if (delete(lastRet)) { // if left-shifted, undo increment in next()
				cursor = (cursor - 1) & (elements.length - 1);
				fence = tail;
			}
			lastRet = -1;
		}
	}

	/**
	 * Returns <tt>true</tt> if this deque contains the specified element.
	 * More formally, returns <tt>true</tt> if and only if this deque contains
	 * at least one element <tt>e</tt> such that <tt>o.equals(e)</tt>.
	 *
	 * @param o object to be checked for containment in this deque
	 * @return <tt>true</tt> if this deque contains the specified element
	 */
	public boolean contains(Object o) {
		if (o == null)
			return false;
		int mask = elements.length - 1;
		int i = head;
		E x;
		while ((x = elements[i]) != null) {
			if (o.equals(x))
				return true;
			i = (i + 1) & mask;
		}
		return false;
	}

	/**
	 * Removes a single instance of the specified element from this deque.
	 * If the deque does not contain the element, it is unchanged.
	 * More formally, removes the first element <tt>e</tt> such that
	 * <tt>o.equals(e)</tt> (if such an element exists).
	 * Returns <tt>true</tt> if this deque contained the specified element
	 * (or equivalently, if this deque changed as a result of the call).
	 * <p/>
	 * <p>This method is equivalent to {@link #removeFirstOccurrence}.
	 *
	 * @param o element to be removed from this deque, if present
	 * @return <tt>true</tt> if this deque contained the specified element
	 */
	public boolean remove(Object o) {
		return removeFirstOccurrence(o);
	}

	/**
	 * Removes all of the elements from this deque.
	 * The deque will be empty after this call returns.
	 */
	public void clear() {
		int h = head;
		int t = tail;
		if (h != t) { // clear all cells
			head = tail = 0;
			int i = h;
			int mask = elements.length - 1;
			do {
				elements[i] = null;
				i = (i + 1) & mask;
			} while (i != t);
		}
	}

	/**
	 * Returns an array containing all of the elements in this deque
	 * in proper sequence (from first to last element).
	 * <p/>
	 * <p>The returned array will be "safe" in that no references to it are
	 * maintained by this deque.  (In other words, this method must allocate
	 * a new array).  The caller is thus free to modify the returned array.
	 * <p/>
	 * <p>This method acts as bridge between array-based and collection-based
	 * APIs.
	 *
	 * @return an array containing all of the elements in this deque
	 */
	public Object[] toArray() {
		return copyElements(new Object[size()]);
	}

	// *** Object methods ***

	/**
	 * Returns a copy of this deque.
	 *
	 * @return a copy of this deque
	 */
	public ArrayDeque<E> clone() {
		throw new UnsupportedOperationException();
	}
}
