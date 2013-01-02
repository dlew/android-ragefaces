from optparse import OptionParser
import os
import shutil
import subprocess

if __name__ == "__main__":
    usage = "usage: %prog [options]"
    parser = OptionParser(usage=usage)
    parser.add_option('-f', '--file', action="store", help="The path to the image you want to add")
    parser.add_option('-c', '--category', action="store", help="The categories it should be added to")

    (options, args) = parser.parse_args()

    faces_directory = os.path.abspath(os.path.join(os.path.dirname( __file__ ), '..', 'faces'))

    if options.file is None:
        parser.error('File not specified (the -f option)')

    shutil.copy(options.file, faces_directory)

    subprocess.Popen( "python " +  os.path.abspath(os.path.join(os.path.dirname( __file__ ), "convert.py")) + " -f " + faces_directory, shell=True)

#1 - Copy to faces/
#1 - convert.py python scripts/convert.py 
#2 - Copy from out/... to /res/...
#3 - Create CSV faces.csv
#4 - db-tools.py python scripts/db-tools.py -f project/assets/faces.db -a faces.csv 
#5 - delete faces.csv

