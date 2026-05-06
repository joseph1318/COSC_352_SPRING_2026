FROM r-base:4.3.1

RUN R -e "install.packages(c('rvest','dplyr','stringr'), repos='https://cran.r-project.org')"

WORKDIR /app
COPY histogram.R .

CMD ["Rscript", "histogram.R"]
