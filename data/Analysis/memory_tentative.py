# # # # Your JSON data
##data_distribution ={"1": 145953585, "2": 28492637, "3": 6457229, "4": 3112731, "5": 1052022, "6": 589531, "7": 231588, "8": 144358, "9": 60774, "10": 39457, "11": 18110, "12": 12706, "13": 5688, "14": 4106, "15": 2177, "16": 1692, "17": 907, "18": 633, "19": 351, "20": 318, "21": 142, "22": 155, "23": 80, "24": 98, "25": 55, "26": 49, "27": 29, "28": 28, "29": 20, "30": 32, "31": 11, "32": 24, "33": 7, "34": 5, "35": 6, "36": 2, "37": 1, "38": 3, "39": 1, "40": 3, "41": 1, "42": 1, "43": 1, "44": 3, "45": 2, "46": 1, "48": 1, "49": 1, "51": 1, "53": 1, "55": 1, "58": 2, "64": 1, "65": 1, "70": 1, "74": 1, "88": 1, "117": 1}

# # def variable_byte_encode(number):
# #     # Variable byte encoding logic
# #     bytes_encoded = []
# #     while True:
# #         byte = number & 0x7F  # Get the lowest 7 bits
# #         bytes_encoded.append(byte)
# #         number >>= 7
# #         if number == 0:
# #             break
# #     bytes_encoded[-1] |= 0x80  # Set the highest bit of the last byte
# #     return bytes_encoded

# # total_bytes = 0

# # for key, value in data.items():
# #     key_integer = int(key)
# #     key_bytes = variable_byte_encode(key_integer)
# #     key_length = len(key_bytes)
# #     total_bytes += key_length * value

# # print("Total space required in bytes FOR VARIABLE BYTE ENCODING:", total_bytes)


# # def unary_encode(number):
# #     # Unary encoding logic
# #     return '1' * number + '0'

# # total_bits = 0

# # for key, value in data.items():
# #     key_integer = int(key)
# #     key_encoded = unary_encode(key_integer)
# #     key_length = len(key_encoded)
# #     total_bits += key_length * value

# # # Calculate the total bytes (8 bits per byte)
# # total_bytes = total_bits // 8

# # print("Total space required in bytes:", total_bytes)


import struct
from varint import encode as varint_encode

# Definiamo la distribuzione dei dati
data_distribution = {
    1: 145953585, 2: 28492637, 3: 6457229, 4: 3112731, 5: 1052022, 6: 589531, 7: 231588, 8: 144358,
    9: 60774, 10: 39457, 11: 18110, 12: 12706, 13: 5688, 14: 4106, 15: 2177, 16: 1692, 17: 907,
    18: 633, 19: 351, 20: 318, 21: 142, 22: 155, 23: 80, 24: 98, 25: 55, 26: 49, 27: 29, 28: 28,
    29: 20, 30: 32, 31: 11, 32: 24, 33: 7, 34: 5, 35: 6, 36: 2, 37: 1, 38: 3, 39: 1, 40: 3,
    41: 1, 42: 1, 43: 1, 44: 3, 45: 2, 46: 1, 48: 1, 49: 1, 51: 1, 53: 1, 55: 1, 58: 2, 64: 1,
    65: 1, 70: 1, 74: 1, 88: 1, 117: 1
}

# Definiamo una funzione per calcolare la dimensione in byte di una sequenza di bytes
def get_byte_size(data):
    return len(data)

# Definiamo una funzione per la codifica unaria
def unary_encode(value):
    return "1" * (value - 1) + "0"

# Calcola le dimensioni con la codifica binaria, variable byte, p for delta e unaria
binary_sizes = {}
varint_sizes = {}
p_for_delta_sizes = {}
unary_sizes = {}

for value, frequency in data_distribution.items():
    # Codifica binaria
    binary_data = struct.pack('I', value) * frequency
    binary_size = get_byte_size(binary_data)
    binary_sizes[value] = binary_size

    # Codifica variable byte
    varint_data = varint_encode(value) * frequency
    varint_size = get_byte_size(varint_data)
    varint_sizes[value] = varint_size

    # Codifica p for delta
    delta_values = [value]
    for i in range(1, frequency):
        delta = value - delta_values[-1]
        delta_values.append(delta)
    p_for_delta_data = b"".join(varint_encode(delta) for delta in delta_values)
    p_for_delta_size = get_byte_size(p_for_delta_data)
    p_for_delta_sizes[value] = p_for_delta_size

    # Codifica unaria
    unary_data = unary_encode(value) * frequency
    unary_size = get_byte_size(unary_data.encode())
    unary_sizes[value] = unary_size

# Stampiamo i risultati
print("Valore\tBinary Size\tVarint Size\tP for Delta Size\tUnary Size")
for value, frequency in data_distribution.items():
    print(f"{value}\t{binary_sizes[value]}\t\t{varint_sizes[value]}\t\t{p_for_delta_sizes[value]}\t\t{unary_sizes[value]}")





# Calcola le dimensioni con la codifica binaria, variable byte, p for delta e unaria
total_binary_size = 0
total_varint_size = 0
total_p_for_delta_size = 0
total_unary_size = 0

for value, frequency in data_distribution.items():
    # Codifica binaria
    binary_data = struct.pack('I', value) * frequency
    binary_size = get_byte_size(binary_data)
    total_binary_size += binary_size

    # Codifica variable byte
    varint_data = varint_encode(value) * frequency
    varint_size = get_byte_size(varint_data)
    total_varint_size += varint_size

    # Codifica p for delta
    delta_values = [value]
    for i in range(1, frequency):
        delta = value - delta_values[-1]
        delta_values.append(delta)
    p_for_delta_data = b"".join(varint_encode(delta) for delta in delta_values)
    p_for_delta_size = get_byte_size(p_for_delta_data)
    total_p_for_delta_size += p_for_delta_size

    # Codifica unaria
    unary_data = unary_encode(value) * frequency
    unary_size = get_byte_size(unary_data.encode())
    total_unary_size += unary_size

# Stampiamo i risultati
print("Encoding\tTotal Size")
print(f"Binary\t\t{total_binary_size}")
print(f"Varint\t\t{total_varint_size}")
print(f"P for Delta\t{total_p_for_delta_size}")
print(f"Unary\t\t{total_unary_size}")

# Confrontiamo le dimensioni per determinare la codifica più efficiente
min_size = min(total_binary_size, total_varint_size, total_p_for_delta_size, total_unary_size)

if min_size == total_binary_size:
    print("La codifica binaria è la più efficiente.")
elif min_size == total_varint_size:
    print("La codifica variable byte (varint) è la più efficiente.")
elif min_size == total_p_for_delta_size:
    print("La codifica p for delta è la più efficiente.")
else:
    print("La codifica unaria è la più efficiente.")
