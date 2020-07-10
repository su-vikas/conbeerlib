# conbeerlib

*conbeerlib* is an Android library for detecting if an app is running inside a **virtual container**.  

This is based on the work done by me and [Gautam](https://github.com/darvincisec), presented at [Android Security Symposium 2020](https://android.ins.jku.at/symposium/program/) - [slides](android_virtual_containers_slides.pdf).

This repo contains *conbeerlib* and a wrapper application, *conware*, which we used for our research.

# Checks

Currently following checks are implemented: 

1. Permissions in Manifest - There can be a mismatch between the permissions granted and originally requested by an application. 
2. Process Memory - Check */proc/self/maps* for presence of artifacts not belonging to the app's expected filepath. 
3. Storage Dir - The assigned storage path for an app inside virtual container is different, as compared to when installed directly on Android device. 
4. Environment Variables - Virtual containers set various environment variables.
5. Running App Services - There can be other services running than what started by an application.
6. App Components - Enable app components dynamically may not always work in virtual containers. 

# Usage

```Java
ConBeer cb = new ConBeer(context, appServiceNames);
if (cb.isContainer()) {
    // container is present
}else{
    // container not present
}
```

Add following code in `onResume()` method of your application. This code is responsible for dynamically enabling a dummy app component, which is used for testing presence of virtual containers. The component is defined in *conbeerlib's* manifest file. 

```Java
ComponentName componentName = new ComponentName(this.getApplicationContext(), FakeBroadcastReceiver.class);
this.getPackageManager().setComponentEnabledSetting(componentName,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP);
```

# Limitations

This library is tested only with a limited number of virtual containers and may not be able to detect all them currently. Also, given the madness of Android device diversity, there is a good chance of false positives. 

Please feel free to open an issue when you encounter such problems.


# License

This project is released under the MIT License.
