from optparse import OptionParser
import os
import shutil
import subprocess
import tempfile
import time



if __name__ == "__main__":
    usage = "usage: %prog [options]"
    parser = OptionParser(usage=usage)
    parser.add_option('-f', '--file', action="store", help="The path to the image you want to add")
    parser.add_option('-c', '--category', action="store", help="The categories it should be added to")

    (options, args) = parser.parse_args()

    faces_directory = os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'faces'))

    if options.file is None:
        parser.error('File not specified (the -f option)')

    print "Copying file to faces directory"
    shutil.copy(options.file, faces_directory)

    print "Converting image"
    subprocess.Popen( "python " +  os.path.abspath(os.path.join(os.path.dirname( __file__ ), "convert.py")) + " -f " + faces_directory, shell=True)

    print "Sleep 3 seconds"
    time.sleep(3)

    filename_without_extension = os.path.splitext(os.path.basename(options.file))[0] 

    out_directory = os.path.abspath(os.path.join(os.path.dirname( __file__ ), 'out'))

    print "Copying file to various DPI directories"

    hdpi_path = os.path.join(out_directory, 'drawable-hdpi',  filename_without_extension + '.jpg')
    hdpi_directory = os.path.join(os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'project/res')), 'drawable-hdpi')
    shutil.copy(hdpi_path, hdpi_directory)

    ldpi_path = os.path.join(out_directory, 'drawable-ldpi',  filename_without_extension + '.jpg')
    ldpi_directory = os.path.join(os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'project/res')), 'drawable-ldpi')
    shutil.copy(ldpi_path, ldpi_directory)

    mdpi_path = os.path.join(out_directory, 'drawable-mdpi',  filename_without_extension + '.jpg')
    mdpi_directory = os.path.join(os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'project/res')), 'drawable-mdpi')
    shutil.copy(mdpi_path, mdpi_directory)

    xhdpi_path = os.path.join(out_directory, 'drawable-xhdpi',  filename_without_extension + '.jpg')
    xhdpi_directory = os.path.join(os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'project/res')), 'drawable-xhdpi')
    shutil.copy(xhdpi_path, xhdpi_directory)

    hdpi_path = os.path.join(out_directory, 'raw',  os.path.basename(options.file))
    hdpi_directory = os.path.join(os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'project/res')), 'raw')
    shutil.copy(hdpi_path, hdpi_directory)

    print "Creating CSV file"
    with open("newface.csv", "w") as text_file:
        text_file.write(filename_without_extension + "," + options.category)

    faces_db = os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'project/assets/faces.db'))

    db_cmd = "python " +  os.path.abspath(os.path.join(os.path.dirname( __file__ ), "db-tools.py")) + " -f " + faces_db + " -a newface.csv"


    print "Sleep 5 seconds"
    time.sleep(5)

    print "Adding to database"
    with tempfile.TemporaryFile() as tmpFile:
        proc = subprocess.Popen( db_cmd, shell=True, stdout=tmpFile.fileno() )
        proc.wait()


    print "Deleting CSV and out directory"
    os.remove("newface.csv")
    shutil.rmtree("out", ignore_errors=True)


#1 - Copy to faces/
#1 - convert.py python scripts/convert.py 
#2 - Copy from out/... to /res/...
#3 - Create CSV faces.csv
#4 - db-tools.py python scripts/db-tools.py -f project/assets/faces.db -a faces.csv 
#5 - delete faces.csv

