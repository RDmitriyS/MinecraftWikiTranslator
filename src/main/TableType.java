package main;

public enum TableType {
    Default,
    Template,
    InvSprite,
    BlockSprite,
    ItemSprite,
    BiomeSprite,
    EnvSprite,
    EffectSprite,
    EntitySprite;

    String getModuleName(Lang lang) {
        if (!isModule()) {
            throw new IllegalStateException(name() + " is not a module");
        }
        if (lang == Lang.en) {
            return name();
        } else {
            switch (this) {
                case InvSprite:
                    return "ИнвСпрайт";
                case BlockSprite:
                    return "Спрайт/Блок";
                case ItemSprite:
                    return "Спрайт/Предмет";
                case BiomeSprite:
                    return "Спрайт/Биом";
                case EnvSprite:
                    return "Спрайт/Окружение";
                case EffectSprite:
                    return "Спрайт/Эффект";
                case EntitySprite:
                    return "Спрайт/Сущность";
            }
        }
        throw new IllegalStateException(name() + " doesn't have a module name");
    }

    boolean isModule() {
        return this != Default && this != Template;
    }
}
