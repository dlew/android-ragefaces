Android Rage Faces
------------------

This is the source code for the free Android application "Rage Faces".

Enjoy.  Or don't enjoy.  Whatever.

Adding Faces
------------

The build process for new faces go along two paths: easy (lacking features) and hard.

**EASY MODE**: Place JPG images into the /project/res/raw/ directory, and delete
/project/assets/faces.db.  It is easy to add new faces, but you won't be
able to filter.  Also, performance may be degraded due to the size of the images
being loaded into memory.

In addition, I haven't tested this version in ages.  It may be buggy.

**HARD MODE**: This version is based off of faces.db, as well as thumbnail drawables.
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
