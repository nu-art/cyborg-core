## What is Cyborg?

If you are an Android developer then you must know the pain of an Android Application birth, the logs, the screens, the lists, view pagers, adapters, activities, fragments and just the thought about all this code you need to write(or copy) AGAIN, is simply depressing! (Boiler plate anyone)

Cyborg allows you to quickly develop an Android application using an injection mechanism, and a module based architecture, that put together saves a lot of code, time and Android agony.

The amount of code required to use Cyborg in comparison to Android, or other Android frameworks is infinitesimal, it encapsulate actions, callbacks, dispatching events, large repetitive common logical flows and enforces proper Java architecture, while giving you the freedom to write any logic you like.

The module based architecture allows Cyborg to wrap any business logic you need for your application, and any function of Android, with already dozens of modules implemented(Which are currently not a part of this package), e.g. Ads, Bluetooth, Automation recording, Crash report, Google Analytics, In-App purchases, Location, Share/Feedback/Rate, and planty more.

This sounds like magic... well, it is not, it is years of hard work, that is finally brought to the surface.

 

## Cyborg has real MVC
Cyborg defines things a bit differently to overcome some of Android design flaws:
 - Module driven development as the Model.
 - Views are the only UI components.
 - Controllers are in place to manage UI behavior and bridge between the Views and the Model

