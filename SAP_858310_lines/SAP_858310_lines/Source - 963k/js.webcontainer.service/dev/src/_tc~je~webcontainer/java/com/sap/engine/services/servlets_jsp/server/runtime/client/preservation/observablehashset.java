package com.sap.engine.services.servlets_jsp.server.runtime.client.preservation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;


public class ObservableHashSet<E>  extends Observable implements Set<E>{
	private HashSet<E> set = new HashSet<E>();
	private ReentrantLock lock = new ReentrantLock();
	
	public boolean add(final E e) {
		try {
			lock.lock();
		
			if (e == null){
				throw new NullPointerException("The specified parameter is null");
			}
			if (set.contains(e)){
				return false;
			}
			set.add(e);
		}finally{
			lock.unlock();
		}
		return true;
	}

	public boolean addAll(final Collection<? extends E> c) {
		boolean result = false;
		try{
			lock.lock();
		
			if (c == null){
				throw new NullPointerException("The specified parameter is null");
			}
			Iterator <? extends E> iter = c.iterator();
			while (iter != null && iter.hasNext()){
				boolean t = add(iter.next());
				if (t){
					result = false;
				}
			}
		}finally{
			lock.unlock();
		}
		return result;
	}

	public void clear() {
		try{
			lock.lock();
			set.clear();
		}finally{
			lock.unlock();
		}
		if (this.countObservers() > 0 ){
			this.setChanged();
			this.notifyObservers();
		}

	}

	public boolean contains(final Object o) {
		if (o == null){
			return false;
		}
		else{
			return set.contains(o);
		}
	}

	public boolean containsAll(Collection<?> c) {
		boolean result = false;
		if (c == null){
			return false;
		}
		
		try{
			lock.lock();
			Iterator<?> iter = c.iterator();
			while (iter != null && iter.hasNext()){
				if (!set.contains(iter.next())){
					break;
				}
			result = true;
			}
		}finally{
			lock.unlock();
		}
		return result;
	}

	public boolean isEmpty() {
		try{
			lock.lock();
			return set.isEmpty();
		}finally{
			lock.unlock();
		}
		
		
	}

	public boolean remove(final Object o) {
		boolean result = false;
		if (o == null){
			return false;
		}
			try{
				lock.lock();
			result = set.remove(o);
			}finally{
				lock.unlock();
			}
			if (set.isEmpty()){
				if (this.countObservers() > 0 ){
					this.setChanged();
					this.notifyObservers();
				}
				result = true;
			}
		return result;
	}

	public boolean removeAll(final Collection<?> c) {
		if (c == null){
			return false;
		}
		boolean result = false;
		try{
			lock.lock();
			Iterator<?> iter = c.iterator();
			while(iter != null && iter.hasNext()){
				boolean t = remove(iter.next());
				if (t){
					result = true;
				}
			}
		}finally{
			lock.unlock();
		}
		return result;
	}

	public boolean retainAll(final Collection<?> c) {
		if (c == null){
			return false;
		}
		boolean result = false;
		if (set == null || set.isEmpty()){
			return false;
		}
		try{
			lock.lock();
			ArrayList<E> toRemove = new ArrayList<E>();
			Iterator<E> iter = set.iterator();
			while (iter.hasNext()){
				E type = iter.next();
				if (!c.contains(type)){
					toRemove.add(type);
				}
			}
			if (toRemove.size()>0){
				result = set.removeAll(toRemove);
			}
		}finally{
			lock.unlock();
		}
		return result;
	}

	public Object[] toArray() {
		try{
			lock.lock();
			ArrayList<E> list = new ArrayList<E>(set.size());
			list.addAll(set);
			return list.toArray();
		}finally{
			lock.unlock();
		}
	}

	public <T> T[] toArray(T[] a) {
		try{
			lock.lock();
			ArrayList<E> list = new ArrayList<E>(set.size());
			list.addAll(set);
			return list.toArray(a);
		}finally{
			lock.unlock();
		}
	}

	public Iterator<E> iterator() {
		try{
			lock.lock();
			ArrayList<E> list = new ArrayList<E>(set.size());
			list.addAll(set);
			return list.iterator();
		}finally{
			lock.unlock();
		}
	}

	public int size() {
		try {
			lock.lock();
			return set.size();
		}finally{
			lock.unlock();
		}
		
	}

	public String toString(){
		if (set.isEmpty()){
			return "[empty]";
		}else{
			try{
				lock.lock();
				StringBuilder builder = new StringBuilder();
				Iterator<E> ier = set.iterator();
				builder.append("[");
				while(ier.hasNext()){
					builder.append(ier.next().toString());
					if (ier.hasNext())
						builder.append(";");
				}
				builder.append("]");
				return builder.toString();
			}finally{
				lock.unlock();
			}
		}
	}

	public int getSizeandLock() {
		try{
			lock.lock();
			int result = set.size();
			if (result <= 0){
				lock.unlock();
			}
			return result;
		}finally{
			;
		}
	}
	public void releaseLock(){
		try{
			lock.unlock();
		}finally{
			;
		}
	}
}