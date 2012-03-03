# Reads category/faces files.
# File should be formatted as CSV:
# <name>,<cat1>,<cat2>,...,<catn>

import sys
import os
import sqlite3
import csv
from optparse import OptionParser

DEFAULT_DB_FILE = "faces.db"

# We localize all the categories now; this is used to upgrade a DB to use the
# new localization, as well as converting new entries into the proper strings.
CATEGORIES = {
	"Happy": "category_happy",
	"Laughing": "category_laughing",
	"Sad": "category_sad",
	"Angry": "category_angry",
	"Rage": "category_rage",
	"Troll": "category_troll",
	"Horror": "category_horror",
	"Neutral": "category_neutral",
	"Thinking": "category_thinking",
	"Why": "category_why",
	"Seriously?": "category_seriously",
	"Challenged": "category_challenged",
	"Disappointed": "category_disappointed",
	"Awe": "category_awe",
	"Cereal": "category_cereal",
	"Okay": "category_okay",
	"Forever Alone": "category_forever_alone",
	"Pleasure": "category_pleasure",
	"Me Gusta": "category_me_gusta",
	"Poker Face": "category_poker_face",
	"Shit": "category_shit",
	"Inglip": "category_inglip",
	"Animal": "category_animal",
	"Tears": "category_tears",
	"Surprised": "category_surprised",
	"Fuck Yea": "category_fuck_yea",
	"Milk": "category_milk",
	"Drunk": "category_drunk",
	"Reddit": "category_reddit",
	"Celebrity": "category_celebrity",
	"Stick Figure": "category_stick_figure",
	"Dad": "category_dad",
	"Grandma": "category_grandma",
	"Male": "category_male",
	"Female": "category_female",
	"Text": "category_text",
	"No Text": "category_no_text",
	"Evil": "category_evil",
	"Trees": "category_trees",
	"Victory": "category_victory"
}

# Used if the DB doesn't even exist
def create_db(db):
    print("Creating initial db schema...")
    c = db.cursor()
    c.execute("CREATE TABLE Categories (_id INTEGER PRIMARY KEY, category TEXT, position INTEGER);")
    c.execute("CREATE TABLE Faces (_id INTEGER PRIMARY KEY, drawable TEXT);")
    c.execute("CREATE TABLE FaceCategories (faceId INTEGER, categoryId INTEGER);")
    c.execute("CREATE TABLE android_metadata (locale TEXT DEFAULT 'en_US');")
    c.execute("INSERT INTO android_metadata VALUES('en_US');")
    db.commit()

def localize_db(db):
    print("Localizing the db...")

    c = db.cursor()

    for category in CATEGORIES:
        c.execute("UPDATE Categories SET category=? WHERE category=?", (CATEGORIES[category], category))

    # Commit all changes
    db.commit()
    c.close()

def print_category_strings():
    for category in CATEGORIES:
        print('<!-- Category label for "%s" -->' % category)
        print('<string name="%s">%s</string>' % (CATEGORIES[category], category))

# Reads a faces/categories file, adds it to the db
#
# File format is:
# <filename>,<category-1>,...,<category-n>
def read_file(infile, db):
    print("Adding faces from '%s'..." % infile)

    c = db.cursor()

    # Read the file
    reader = csv.reader(open(infile, 'rb'))
    data = []
    categories = {}
    for row in reader:
        data.append(row)
        for a in range(1,len(row)):
            category = row[a]
            if category not in categories:
                categories[category] = None

    # Figure out the category ids (if does not exist, prompt user to continue or not)
    for category in categories:
        if category not in CATEGORIES:
            print("ERROR: Make sure we've got a localization for the category '%s'" % category)
            sys.exit()
        loc_category = CATEGORIES[category]
        
        c.execute("SELECT _id FROM Categories WHERE category=?", (loc_category,))
        row = c.fetchone()
        if row is None:
            print('Category listed that does not yet exist - "%s".  Enter "y" to create, anything else to cancel run.' % category)
            yes_no = raw_input("[y/n] ")
            if yes_no == 'y':
                c.execute("INSERT INTO Categories (category) VALUES (?)", (loc_category,))
                categories[category] = c.lastrowid
                print("IMPORTANT: BE SURE TO ADD A PRIORITY MANUALLY FOR THIS NEW CATEGORY!")
            else:
                sys.exit()
        else:
            categories[category] = row[0]

    # Add the faces to the db (that aren't already in there)
    faces = {}
    for row in data:
        face = row[0]

        # Check if it's already in the db
        c.execute("SELECT _id FROM Faces WHERE drawable=?", (face,))
        row = c.fetchone()
        if row is None:
            print("Adding face %s" % face)
            c.execute("INSERT INTO Faces (drawable) VALUES (?)", (face,))
            faces[face] = c.lastrowid
        else:
            faces[face] = row[0]

    # Link those faces to categories
    for row in data:
        face_id = faces[row[0]]
        for a in range(1, len(row)):
            category_id = categories[row[a]]
            c.execute("INSERT INTO FaceCategories (faceId, categoryId) VALUES (?, ?)", (face_id, category_id))

    # Commit all changes
    db.commit()
    c.close()

if __name__ == "__main__":
    usage = "usage: %prog infile [options]"
    parser = OptionParser(usage=usage)
    parser.add_option('-f', '--file', action="store", help="Target file for database", default=DEFAULT_DB_FILE)
    parser.add_option('-a', '--add', action="store", help="CSV file to add to database")
    parser.add_option('-p', '--print_cats', action="store_true", help="Print out localized category strings")

    (options, args) = parser.parse_args()

    db_exists = os.path.exists(options.file)
    db = sqlite3.connect(options.file)
    if not db_exists:
        create_db(db)

    localize_db(db)
    if (options.print_cats):
        print_category_strings()

    if (options.add):
        data = read_file(options.add, db)
