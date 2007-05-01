#!/usr/bin/ruby

DIST_DIR="target/dist"

Dir.glob("#{DIST_DIR}/*.{zip,gz,bz2}") do |filename|
  puts filename
  # ... and you have to provide your passphrase again and again!
  system "gpg --armor --output #{filename}.asc --detach-sig #{filename}"
end

puts "Uploading distributions ..."

system 'scp #{DIST_DIR}/* hlship@people.apache.org:public_html/tapestry-releases'