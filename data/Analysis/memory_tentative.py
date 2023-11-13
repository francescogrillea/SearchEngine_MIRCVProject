import struct
import json

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
    return len(data) * 8


# Definiamo una funzione per la codifica unaria
def unary_encode(value):
    return value


def VariableByteEncodeNumber(n):
    byte = []
    while True:
        byte.append(n % 128)
        if n < 128:
            break
        n //= 128
    byte[0] += 128
    return byte[::-1]

# Calcola le dimensioni con la codifica binaria, variable byte, p for delta, elias fano e unaria
varint_sizes = {}
unary_sizes = {}

for value, frequency in data_distribution.items():
    # Codifica variable byte
    varint_data = VariableByteEncodeNumber(value) * frequency
    varint_size = get_byte_size(varint_data)
    varint_sizes[value] = varint_size

    # Codifica unaria
    unary_data = unary_encode(value) * frequency
    unary_size = unary_data
    unary_sizes[value] = unary_size

# Calcola le dimensioni totali per ciascuna codifica
total_varint_size = sum(varint_sizes.values())
total_unary_size = sum(unary_sizes.values())

# Identifica la codifica con la dimensione minima
min_size_encoding = min(
    ("Variable Byte", total_varint_size),
    ("Unary", total_unary_size),
    key=lambda x: x[1]
)

# Creiamo un dizionario per i risultati
results = {
    "Variable Byte": {
        "Total Size": total_varint_size,
        "Lengths": varint_sizes
    },
    "Unary": {
        "Total Size": total_unary_size,
        "Lengths": unary_sizes
    },
    "Min Size Encoding": min_size_encoding[0]
}

# Scriviamo il dizionario in un file JSON
with open('output.json', 'w') as json_file:
    json.dump(results, json_file, indent=2)

print("Output written to 'output.json'")
