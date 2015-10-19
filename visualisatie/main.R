askForDataFile <- function(){
  file <- file.choose();
  return(file)
}

readDataFile <- function(inputFile){
  inputData <- read.csv(inputFile, header=TRUE, sep=',', dec=".");
  return(inputData);
}

handleData <- function(inputData){
# Create a summary of the data
# http://www.cookbook-r.com/Graphs/Plotting_means_and_error_bars_(ggplot2)/
}

handleFile <- function(filePath){
  inputData <- readDataFile(filePath);
  display(inputData);  
}

mergingTraffic<-function(){
  data <- handleFile("../TrafficDemo/output/test.csv");
}

main <- function(){
  mergingTraffic();
}

main()

