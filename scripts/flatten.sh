#!/bin/bash

# Simple script for flattening all images in a directory
#
# Usage: $ ./flatten.sh <directory>
#
# Depends on ImageMagick, must have that installed/working

for file in $1/*
do
	echo "Flattening $file..."
	convert "$file" -background white -flatten +matte "$file"
done
