package de.headshotharp.obj;

public class UserData<T> {
	private String name;
	private T data;

	public UserData(String name, T data) {
		this.name = name;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
