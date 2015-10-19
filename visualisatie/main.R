library(ggplot2)

source("./globals.R");

askForDataFile <- function(){
  file <- file.choose();
  return(file)
}

readDataFile <- function(inputFile){
  inputData <- read.csv(inputFile, header=TRUE, sep=',', dec=".");
  return(inputData);
}


summarySE <- function(data=NULL, measurevar, groupvars=NULL, na.rm=FALSE,
                      conf.interval=.95, .drop=TRUE) {
  library(plyr)
  
  # New version of length which can handle NA's: if na.rm==T, don't count them
  length2 <- function (x, na.rm=FALSE) {
    if (na.rm) sum(!is.na(x))
    else       length(x)
  }
  
  # This does the summary. For each group's data frame, return a vector with
  # N, mean, and sd
  datac <- ddply(data, groupvars, .drop=.drop,
                 .fun = function(xx, col) {
                   c(N    = length2(xx[[col]], na.rm=na.rm),
                     mean = mean   (xx[[col]], na.rm=na.rm),
                     sd   = sd     (xx[[col]], na.rm=na.rm)
                   )
                 },
                 measurevar
  )
  
  # Rename the "mean" column    
  datac <- rename(datac, c("mean" = measurevar))
  
  datac$se <- datac$sd / sqrt(datac$N)  # Calculate standard error of the mean
  
  # Confidence interval multiplier for standard error
  # Calculate t-statistic for confidence interval: 
  # e.g., if conf.interval is .95, use .975 (above/below), and use df=N-1
  ciMult <- qt(conf.interval/2 + .5, datac$N-1)
  datac$ci <- datac$se * ciMult
  
  return(datac)
}


plotSpeedAsFunctionOfRatio <- function(dataToPlot, outputFile){
  plot = ggplot(data = dataToPlot, aes(x=ratioAutonomousCars, y=speedMS, colour=Driver)) + 
    geom_errorbar(aes(ymin=speedMS-se, ymax=speedMS+se), width=.1) +
    geom_line() + 
    geom_point() + 
    xlab("Ratio of autonomous to human drivers") + 
    ylab("Speed (m/s)") + 
    ylim(0.10, 0.20) + 
    scale_colour_hue(name="Driver",   
                     breaks=c("AutonomousDriver", "HumanDriver"),
                     labels=c("Autnomous", "Human"));
  file.path()
  print(plot);
    ggsave(
    filename = file.path("../report/img", outputFile),
    plot = plot,
    width=tex.textwidth, height=(tex.textwidth/2) - 1, unit="cm"
  )
}

prepareData <- function(inputData, pathLengths, subsets){
  # Prepare data for plotting
  findPathLength <- function(x){
    return(pathLengths[as.character(x)][[1]]);
  }
  
  computeSpeed <- function(distance, time){
    return(distance / time);
  }
  
  inputData$pathLength = lapply(inputData$Route, findPathLength);
  inputData$speedMS = as.numeric(mapply(computeSpeed, inputData$DrivingTime, inputData$pathLength));
  inputData$ratioAutonomousCars = as.factor(inputData$ratioAutonomousCars);
  
  
  for (route in subsets){
    dataForRoute <- subset(inputData, inputData$Route %in% unlist(route$factor));
    dataForRouteSummary <- summarySE(
      data=dataForRoute, 
      measurevar="speedMS", 
      groupvars=c("ratioAutonomousCars", "Driver")
    );
    plotSpeedAsFunctionOfRatio(dataForRouteSummary, route['fileName']);
  }  
}

handleFile <- function(filePath, pathLengths){
  inputData <- readDataFile(filePath);
}

mergingTraffic<-function(){
  pathLengths <- list(
    `From 0 to 3` = 100 + 100,    
    `From 1 to 3` = 100 + sqrt(100 ^2 + 100^2)
  )
  subsets <- list( 
    list(
      factor = "From 0 to 3",
      fileName = "merging_03.png"
    ), 
    list(
      factor = "From 1 to 3",
      fileName = "merging_13.png"
    ),
    list(
      factor = list("From 0 to 3", "From 1 to 3"),
      fileName = "merging.png"
    )
  );
  inputData <- handleFile("../TrafficDemo/output/merging_test.csv");
  prepareData(inputData, pathLengths, subsets)
}

intersectingTraffic<-function(){
  pathLengths <- list(
    `From 0 to 3` = 100 + 100,    
    `From 3 to 0` = 100 + 100,        
    `From 1 to 3` = 100 + 100,
    `From 3 to 1` = 100 + 100,    
    `From 0 to 1` = 100 + 100,
    `From 1 to 0` = 100 + 100
  )
  subsets <- list( 
    list(
      factor = list("From 0 to 3", "From 0 to 1", "From 1 to 0", "From 3 to 0"),
      fileName = "intersecting_03_01.png"
    ), 
    list(
      factor = list("From 1 to 3", "From 3 to 1"),
      fileName = "intersecting_13.png"
    ), 
    list(
      factor = list("From 0 to 3", "From 0 to 1", "From 1 to 0", "From 3 to 0", "From 1 to 3", "From 3 to 1"),
      fileName = "intersecting.png"
    )
  );
  inputData <- handleFile("../TrafficDemo/output/intersecting_test.csv");
  prepareData(inputData, pathLengths, subsets)
}

main <- function(){
  mergingTraffic();
  intersectingTraffic();
}

main()

