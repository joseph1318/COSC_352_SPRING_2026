library(rvest)
library(dplyr)
library(stringr)

url <- "https://chamspage.blogspot.com/2025/01/2025-baltimore-city-homicide-list.html"

page <- read_html(url)

tables <- page %>% html_nodes("table") %>% html_table(fill = TRUE)

df <- bind_rows(tables)

# Clean up column names
colnames(df) <- make.names(colnames(df), unique = TRUE)

# Try to find age column
age_col <- grep("age|Age", colnames(df), value = TRUE)[1]

if (!is.na(age_col)) {
  ages <- as.numeric(df[[age_col]])
  ages <- ages[!is.na(ages) & ages > 0 & ages < 120]

  cat("\n=== Victim Age Distribution ===\n")
  age_breaks <- c(0,10,20,30,40,50,60,70,80,100)
  age_labels <- c("0-9","10-19","20-29","30-39","40-49","50-59","60-69","70-79","80+")
  age_groups <- cut(ages, breaks=age_breaks, labels=age_labels, right=FALSE)
  tbl <- table(age_groups)
  print(as.data.frame(tbl))

  png("histogram.png", width=800, height=600)
  barplot(tbl,
          main="2025 Baltimore City Homicide Victims by Age Group",
          xlab="Age Group",
          ylab="Number of Victims",
          col="steelblue",
          border="white")
  dev.off()
  cat("\nHistogram saved to histogram.png\n")
} else {
  cat("Could not find age column. Columns found:\n")
  print(colnames(df))
}