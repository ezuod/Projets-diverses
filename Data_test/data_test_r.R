# Charger librairy
library(tidyverse)

# Renommer la variable de data_1 "indentiffiant" en "identifiant"
names(data_1)[names(data_1) == "identiffiant"] <- "identifiant"

# Merge data_1 et data_2
data_merge <- merge(data_1,data_2,by = "identifiant", all = TRUE)

# Affiche les noms de variables
names(data_merge)