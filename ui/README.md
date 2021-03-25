# Health UI

Even though MicroProfile Health API is build for System to System use, it's still nice to look at the output of /health. 
This library gives you a small web gui on top of ```/health```

## Adding Health UI

In ```pom.xml```
    
```xml

    <dependency>
        <groupId>io.smallrye</groupId>
        <artifactId>smallrye-health-ui</artifactId>
        <version>XXXXX</version>
        <scope>runtime</scope>
    </dependency>    

```

Then go to /<context_root>/health-ui, eg: http://localhost:8080/health-example/health-ui/

## Example screenshot

### Dashboard

![](https://raw.githubusercontent.com/smallrye/smallrye-health/3.0.x/ui/screenshot.png)

### Settings (to refresh automatically etc.)

![](https://raw.githubusercontent.com/smallrye/smallrye-health/3.0.x/ui/screenshot_settings.png)
