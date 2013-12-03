package com.sisemb.falldetector;

import java.util.HashSet;
import java.util.Set;

public class Observable {
	
	public Observable() {
		_observers = new HashSet<Observer>();
	}

	public void addObserver(Observer observer) {
		_observers.add(observer);
	}
	
	public void removeObserver(Observer observer) {
		_observers.remove(observer);
	}
	
	protected void notifyObservers() {
		for (Observer obs : _observers) {
			obs.update();
		}
	}
	
	private Set<Observer> _observers;
}
