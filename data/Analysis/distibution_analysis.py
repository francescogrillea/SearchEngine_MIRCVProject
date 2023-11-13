# import json
# import matplotlib.pyplot as plt
# import numpy as np  # Import numpy for the logarithm function

# # Load data from the JSON file
# with open('term_frequency.json', 'r') as json_file:
#     data = json.load(json_file)

# # Extract integers and frequencies
# integers = [int(key) for key in data.keys()]
# frequencies = [int(value) for value in data.values()]

# # Calculate the logarithm of the value+1
# log_frequencies = [np.log(value + 1) for value in frequencies]

# # Create a histogram
# plt.figure(figsize=(10, 6))
# plt.bar(integers, log_frequencies)  # Display log values
# plt.xlabel('Integer Values')
# plt.ylabel('Logarithm of Frequency (value+1)')
# plt.title('Logarithm of Integer Distribution (value+1)')

# # Show the plot
# plt.show()

# import json
# import matplotlib.pyplot as plt
# import numpy as np
# from scipy.optimize import curve_fit

# # Load data from the JSON file
# with open('term_frequency.json', 'r') as json_file:
#     data = json.load(json_file)

# # Extract integers and frequencies
# integers = np.array([int(key) for key in data.keys()])
# frequencies = np.array([int(value) for value in data.values()])

# # Calculate the logarithm of the value+1
# log_frequencies = np.log(frequencies + 1)

# # Create a histogram
# plt.figure(figsize=(10, 6))
# plt.bar(integers, log_frequencies, width=0.8)  # Display log values
# plt.xlabel(r'Integer Values')
# plt.ylabel(r'Logarithm of Frequency ($\log(value+1)$)')
# plt.title(r'Logarithm of Integer Distribution ($\log(value+1)$)')

# # Define the function for a Gaussian distribution
# def gaussian(x, A, mu, sigma):
#     return A * np.exp(-(x - mu)**2 / (2 * sigma**2))

# # Perform curve fitting
# popt, _ = curve_fit(gaussian, integers, log_frequencies)

# # Extract the parameters from the fit
# A_fit, mu_fit, sigma_fit = popt

# # Create a curve based on the fitted parameters
# curve_x = np.linspace(integers.min(), integers.max(), 1000)
# curve_y = gaussian(curve_x, A_fit, mu_fit, sigma_fit)

# # Plot the fitted curve
# plt.plot(curve_x, curve_y, 'r-', label=r'Fitted Gaussian: $A e^{-(x - \mu)^2 / (2\sigma^2)}$' + f'\n$A = {A_fit:.2f}$, $\mu = {mu_fit:.2f}$, $\sigma = {sigma_fit:.2f}$')

# # Show the plot with the legend
# plt.legend()
# plt.show()


import json
import matplotlib.pyplot as plt
import numpy as np

# Load data from the JSON file
with open('term_frequency.json', 'r') as json_file:
    data = json.load(json_file)

# Extract integers and frequencies
integers = np.array([int(key) for key in data.keys()])

# Count the total number of elements
total_elements = len(integers)

# Count the number of elements that do not fall between 1 and 3
elements_not_between_1_and_3 = np.logical_or(integers < 1, integers > 3)
count_not_between_1_and_3 = np.sum(elements_not_between_1_and_3)

# Print the total and count of elements
print(f"Total number of elements: {total_elements}")
print(f"Number of elements not between 1 and 3: {count_not_between_1_and_3}")

# Create a histogram
plt.figure(figsize=(10, 6))
plt.hist(integers, bins=100, color='blue')
plt.xlabel(r'Integer Values')
plt.ylabel('Frequency')
plt.title('Integer Distribution')

# Show the plot
plt.show()
