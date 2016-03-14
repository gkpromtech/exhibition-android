package ru.gkpromtech.exhibition.model;

import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.TableRef;
import ru.gkpromtech.exhibition.model.annotation.Unique;

@TableRef(name="media_favorite", sync=false)
public class MediaFavorite extends Entity {
    public @FK(entity=Media.class, field="id", onDelete="CASCADE", onUpdate="CASCADE") @Null
    @Unique Integer mediaid;

    public Integer favorite;
}
