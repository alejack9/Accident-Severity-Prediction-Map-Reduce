import seaborn as sns
import matplotlib.pyplot as plt
import pandas as pd

continuousIndexes = [        
        "Continuous", "Continuous", "Categorical", "Continuous", "Continuous", "Continuous", "Continuous",
        "Continuous", "Categorical", "Categorical", "Categorical", "Continuous", "Categorical",
        "Continuous", "Categorical", "Continuous", "Categorical", "Continuous", "Continuous", "Categorical",
        "Categorical", "Categorical", "Categorical", "Categorical", "Categorical", "Categorical",
        "Continuous", "Continuous", "Categorical", "Categorical", "Categorical", "Categorical",
        "Categorical", "Categorical", "Categorical", "Categorical", "Categorical", "Categorical",
        "Categorical", "Categorical", "Categorical", "Categorical", "Categorical", "Categorical",
        "Categorical", "Categorical", "Categorical", "Continuous", "Continuous", "Continuous", "Continuous",
        "Categorical"]

continuousIndexes = [i for i, x in enumerate(continuousIndexes) if x == "Continuous"]

print(continuousIndexes)

df1 = pd.read_csv("./data/input_train_1024.csv").drop("Accident_Index", axis=1)
df2 = pd.read_csv("./data/input_train_2048.csv").drop("Accident_Index", axis=1)

for index in continuousIndexes:
    plt.figure(figsize=(8, 4))
    f, axes = plt.subplots(2, 1)
    sns.kdeplot(df1.iloc[:, index], ax=axes[0]).set(title=f"df1 - {df1.columns[index]}")
    sns.kdeplot(df2.iloc[:, index], ax=axes[1]).set(title=f"df2 - {df2.columns[index]}")
    plt.savefig(f"./plots/{df1.columns[index]}.png")
    plt.close()

print(df1.head())
print("============")
print(df2.head())