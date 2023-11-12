# import javaobj
# import os

# frequencies = set()

# def read_java_objects_from_ser(file_path):
#     for filename in os.listdir(file_path):
#         try:
#             print(ser_file_path + filename)
           
#             with open(ser_file_path + filename, 'rb') as ser_file:
#                 deserialized_objects = javaobj.load(ser_file)
#                 lista = deserialized_objects.postingLists

              
                
#                 for i in lista:
#                     posting_list= i.postingList
#                     for post in posting_list:
#                         frequencies.add(post.term_frequency)

#                 with open('freq_analysis.txt', 'w') as file:
#                 # Write a line to the file
#                     file.write(filename +" : " + str(frequencies) + "\n" )
            
            
#         except Exception as e:
#             print(f"An error occurred while reading the .ser file: {e}")
#             return None
 
# if __name__ == "__main__":
#     ser_file_path = "intermediate_postings/" 
#     deserialized_objects = read_java_objects_from_ser(ser_file_path)
 
#     print(frequencies)
       
   
import javaobj
import os

unique_frequencies = set()
frequency_counts = {}

def read_java_objects_from_ser(file_path):
    for filename in os.listdir(file_path):
        try:
            print(ser_file_path + filename)
            with open(ser_file_path + filename, 'rb') as ser_file:
                deserialized_objects = javaobj.load(ser_file)
                lista = deserialized_objects.postingLists

                # Initialize a dictionary to store frequency counts for each file
                file_frequencies = {}

                for i in lista:
                    posting_list = i.postingList
                    for post in posting_list:
                        term_frequency = post.term_frequency
                        unique_frequencies.add(term_frequency)  # Add the unique frequency
                        if term_frequency in file_frequencies:
                            file_frequencies[term_frequency] += 1
                        else:
                            file_frequencies[term_frequency] = 1

                with open('freq_analysis.txt', 'a') as file:  # Use 'a' to append to the file
                    # Write frequency counts for the current file
                    file.write(filename + " : " + str(file_frequencies) + "\n")
            
        except Exception as e:
            print(f"An error occurred while reading the .ser file: {e}")
            return None

if __name__ == "__main__":
    ser_file_path = "intermediate_postings/"
    deserialized_objects = read_java_objects_from_ser(ser_file_path)
    
    print("Unique Frequencies:", unique_frequencies)
