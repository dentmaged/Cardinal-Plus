package in.twizmwaz.cardinal.module.modules.bossBar;

import in.twizmwaz.cardinal.match.Match;
import in.twizmwaz.cardinal.module.BuilderData;
import in.twizmwaz.cardinal.module.ModuleBuilder;
import in.twizmwaz.cardinal.module.ModuleCollection;
import in.twizmwaz.cardinal.module.ModuleLoadTime;

public class BossBarBuilder implements ModuleBuilder {

    @Override
    public ModuleCollection load(Match match) {
        return new ModuleCollection<BossBar>(new BossBar());
    }
}
