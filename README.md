# What is Cyborg?

Cyborg is a framework layer over Android, one that will save you at least 50% of development time, code and Android bugs that you are used to while developing "pure" Android.

If you are an Android developer then you know the pain of an Android Application birth, the logs, the screens, the lists, view pagers, recyclers, adapters, activities, fragments, services, intents and just the thought about all this code you need to write(or copy) AGAIN, is simply depressing! (Boiler plate anyone)

The amount of code required to use Cyborg in comparison to Android, or other Android frameworks is infinitesimal, it encapsulate actions, callbacks, dispatching events, large repetitive common logical flows and enforces proper Java architecture, while giving you the freedom to write any logic you like. 

The module based architecture allows Cyborg to wrap any business logic you need for your application, and any function of Android, with already dozens of modules implemented(Which are currently not a part of this package), e.g. Ads, Bluetooth, Automation recording, Crash report, Google Analytics, In-App purchases, Location, Share/Feedback/Rate, and planty more.


### Cyborg has a real MVVM
Cyborg defines things a bit differently to overcome some of Android design flaws:
 - (**M**)  Module driven development.
 - (**V**)  Views are the only UI components.
 - (**VM**) Controllers are in place to manage UI behavior and bridge between the Views and the Model


## Cyborg's other benefits:
 - Remove completely the Fragment's infamous IllegalStateException(by removing Fragments).
 - Reduce drastically any possibility for memory leaks, context leaks crashes.
 - Compile type safe enforcement by using generics wherever possible.
 - Recycler views with multiple item types in few lines of code.
 - Super convenient API from all of the main components.
 - Custon attributes in a layout xml applicable to Controllers (or any other Object you'd want).
 - Minimize the use of Activities.
 - Minimize the use of Services.
 - Logs made so........ simple.
 - Controller stacks can live in parallel (This is not clear I know!).
 - Preferences are now one liners, to set and get.
 - Communication between components without overhead (This is not clear I know!).
 - Notifications posting and interaction handling encpsulated.
 - Crash report data collection built-in.
 - User scenario recording is a feature!
 - WebView simlified.
 - Print logs to file as a feature.
 - I can probably go on a while longer...


Using the above, Cyborg allows you to quickly develop Android applications and save you a lot of code, time and Android agony.

Sounds like magic...? well, it is not, it is years of hard work, that is finally brought to the light.
 


