import pandas as pd


for size in [1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152]:
    pd.read_pickle(f"./data/input_train_{size}.pkl").to_csv(f"./data/input_train_{size}.csv")
    pd.read_pickle(f"./data/input_test_{size}.pkl").to_csv(f"./data/input_test_{size}.csv")
