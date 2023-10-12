package io.github.jwdeveloper.tiktok.data.models.gifts;

public enum GiftSendType
{
    Finished,
    Begin,
    Active;


    public static GiftSendType fromNumber(long number)
    {
        return switch ((int) number) {
            case 0 -> GiftSendType.Finished;
            case 1, 2, 4 -> GiftSendType.Active;
            default -> GiftSendType.Finished;
        };
    }
}
