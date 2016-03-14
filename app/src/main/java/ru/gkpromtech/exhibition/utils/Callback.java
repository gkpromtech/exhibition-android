package ru.gkpromtech.exhibition.utils;

public interface Callback<T> {
	public void onSuccess(T data) throws Exception;
	public void onError(Throwable exception);
}
