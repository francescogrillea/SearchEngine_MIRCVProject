#!/bin/bash

# Set the path to your data folder
data_folder="."

# Delete specified files in the data folder
rm -f "$data_folder/index.bin" "$data_folder/lexicon.bin" "$data_folder/doc_index.bin"

# Delete all files in the intermediate_posting directory and its subdirectories
intermediate_posting_folder="$data_folder/intermediate_postings"
if [ -d "$intermediate_posting_folder" ]; then
    find "$intermediate_posting_folder" -type f -delete
fi

echo "Deletion completed."
