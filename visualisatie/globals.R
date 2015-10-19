# Settings for the plots
colours = list(
  red 	= "#AF3547",
  blue   	= "#0074BC"
)
font.size = 10;
font.family ="Garamond";

tex.textwidth = 14.92038; #cm

resetPar <- function() {
  dev.new()
  op <- par(no.readonly = TRUE)
  dev.off()
}

printf <- function(...) {
  cat(sprintf(...))
}

reload <- function(){
  rm(list = ls())
  source('globals.R')
  sapply(list.files(pattern="[.]R$", full.names=TRUE), source);
}