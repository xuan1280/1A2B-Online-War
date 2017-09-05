package main;

import java.util.Stack;

public abstract class View {
	private static Stack<View> viewStack = new Stack<>();
	private String name;
	
	public void start() {
		viewStack.push(this);
		name = getViewName();
		onCreate();
		onStart();
	}
	
	public abstract void onCreate();
	
	public abstract void onRecycleActions();
	
	public abstract String getViewName();
	
	public void onRestart() {
		System.out.println(name + " View restarting..�C");
	}
	
	public void onStart() {
		while(!viewStack.isEmpty() && isAlive())
			onRecycleActions();
	}
	
	
	public void finish() {
		viewStack.pop();
		onDestroy();
		
		if (viewStack.isEmpty())
			System.out.println("Test over.");
		else
			viewStack.peek().onRestart();
	}
	
	public abstract void onDestroy();
	
	public boolean isAlive() {
		return viewStack.lastElement() == this;
	}
}
