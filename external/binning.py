import pandas as pd


doBinning = [        
        True, True, False, True, True, True, False,
        True, False, False, False, True, False,
        False, False, False, False, False, False, False,
        False, False, False, False, False, False,
        True, False, False, False, False, False,
        False, False, False, False, False, False,
        False, False, False, False, False, False,
        False, False, False, True, True, True, False,
        False]

# print(len(continuousIndexes))

indexToBin = [i for i, x in enumerate(doBinning) if x == True]

# size = 1024
# train_df = pd.read_csv(f"./data/input_train_{size}.csv", index_col=0)
# for index in indexToBin:
#     print(train_df.columns[index])
#     print(train_df.iloc[:, index].value_counts())

sizes = [1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152]

for size in sizes:
    print(size)
    train_df = pd.read_pickle(f"./data/input_train_{size}.pkl")
    test_df = pd.read_pickle(f"./data/input_test_{size}.pkl")
    for index in indexToBin:
        # print(f"BEFORE {train_df.columns[index]}")
        # print(train_df.iloc[:, index].value_counts())
        train_df[train_df.columns[index]], train_bins = pd.qcut(train_df.iloc[:, index], q=6, duplicates="drop", labels=False, retbins=True)
        test_df[test_df.columns[index]] = pd.cut(test_df.iloc[:, index].clip(upper=train_bins[-1]), bins=train_bins, labels=False, include_lowest=True)
        # print("AFTER")
        # print(train_bins)
        # print(train_df.iloc[:, index].value_counts())
        print("+=======================================+")
    print(train_df.head())
    print("=================")
    print(test_df.head())
    train_df.to_csv(f"./data/input_train_{size}_binned.csv")
    test_df.to_csv(f"./data/input_test_{size}_binned.csv")
