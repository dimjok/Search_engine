package searchengine.sitemap;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.CopyOnWriteArrayList;

@AllArgsConstructor
@Getter
public class SiteMap {
    private String url;
    private CopyOnWriteArrayList<SiteMap> siteMapChildren;

    public SiteMap(String url) {
        siteMapChildren = new CopyOnWriteArrayList<>();
        this.url = url;
    }

    public void addChildren(SiteMap child) {
        siteMapChildren.add(child);
    }
}
