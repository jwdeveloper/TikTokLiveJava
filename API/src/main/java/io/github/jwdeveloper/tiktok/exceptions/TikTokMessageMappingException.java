package io.github.jwdeveloper.tiktok.exceptions;


public class TikTokMessageMappingException extends TikTokLiveException
{
    public TikTokMessageMappingException(Class<?> inputClazz, Class<?> outputClass, Throwable throwable)
    {
        super("Unable to handle mapping from class: " + inputClazz.getSimpleName() + " to class " + outputClass.getSimpleName(),throwable);
    }

    public TikTokMessageMappingException(Class<?> inputClazz, Class<?> outputClass, String message)
    {
        super("Unable to handle mapping from class: " + inputClazz.getSimpleName() + " to class " + outputClass.getSimpleName()+": "+message);
    }
}
