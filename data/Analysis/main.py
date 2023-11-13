import javaobj
import os
import json
import time

frequencies = dict()
# doc_frequencies = set()
BASE_PATH = "intermediate_postings/"


def read_java_objects_from_ser(filename):
    start_time = time.time()
    try:
        with open(BASE_PATH + filename, 'rb') as ser_file:
            deserialized_objects = javaobj.load(ser_file)

            posting_lists = deserialized_objects.postingLists
            n = len(posting_lists)

            for i in range(n):
                posting_list = posting_lists[i].postingList
                # doc_frequencies.add(len(posting_list))
                for posting in posting_list:
                    tf = posting.term_frequency
                    try:
                        frequencies[tf] += 1
                    except KeyError:
                        frequencies[tf] = 1

    except Exception as e:
        print(f"An error occurred while reading the .ser file: {e}")
        return None

    print(f"File {filename} processed in {round(time.time() - start_time, 2)}s")


def int_keys_hook(dictionary):
    return {int(key): value for key, value in dictionary.items()}


# READ ALREADY PROCESSED FILES
try:
    with open('term_frequency.json', 'r') as json_file:
        frequencies = json.load(json_file, object_hook=int_keys_hook)
    # with open('doc_frequency.json', 'r') as json_file:
    #     doc_frequencies = set(json.load(json_file))

except FileNotFoundError:
    pass


FROM_DIR_OFFSET = 0
TO_DIR_OFFSET = 12

# READ .ser FILES
for filename in os.listdir(BASE_PATH[FROM_DIR_OFFSET:]):
    read_java_objects_from_ser(filename)

# SORT TERM FREQUENCY DICTIONARY
sorted_dict = dict(sorted(frequencies.items()))
print(sorted_dict)

# SORT DOCUMENT FREQUENCY DICTIONARY
# doc_frequencies = list(doc_frequencies)
# doc_frequencies = sorted(doc_frequencies)
# print(doc_frequencies)

with open('term_frequency.json', 'w') as json_file:
    json.dump(sorted_dict, json_file)

# with open('doc_frequency.json', 'w') as json_file:
#     json.dump(doc_frequencies, json_file)