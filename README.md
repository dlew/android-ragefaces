Android Rage Faces
===

This is the source code for the free Android application "Rage Faces".

Enjoy.  Or don't enjoy.  Whatever.

Adding Faces
===

The build process for new faces go along two paths: easy and hard.

**EASY MODE**
------------------


Place your PNG anywhere.  Run the `add-image.py` script, specifying the path to your image and the category to add it to.

    python scripts/add-image.py -f /home/username/Desktop/angry_grumpy_cat_good.png -c Angry

`-f` is the path to the image

`-c` is the category to add it to. A list of categories is visible at the [top of this script](https://github.com/mendhak/android-ragefaces/blob/master/scripts/db-tools.py).



**HARD MODE**
---------
This version is based off of faces.db, as well as thumbnail drawables.
It is a multi-step process:

1. Gather source PNG faces.

2. Run them through /scripts/convert.py.  It will spit out both JPEG versions
   (that are shared) and JPEG thumbnails (that are shown in the app itself).
   Add these to the /project/ directory.
   
3. Add new faces to faces.db using /scripts/db-tools.py.  Add the improved
   faces.db to /project/assets/faces.db.  (You could add the faces by hand
   to the db but that's a real pain.)

4. Bump /project/src/com/idunnolol/ragefaces/data/DatabaseHelper.DB_VERSION.
   (The project uses this to determine when to reload faces.db from the APK.)
