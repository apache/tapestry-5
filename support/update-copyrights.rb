#!/usr/bin/ruby -w

# Executable Ruby script to walk one or more directories worth of
# source files and add or update the copyright comment block on each file.
#
# File types are identified by extension, different file types
# vary in the format of the copyright comment block and its exact position.
#
# The template for each block will contain the string {YEAR}.  This is replaced
# by the current year.  However, if a current comment block exists,
# then the file is not touched (ASF rules is that the copyright year should
# only be updated when an actual change occurs to the file).
#
# The {ORG} placeholder in the template is replaced with the organization,
# which defaults to "The Apache Software Foundation", but can be overridden with the
# -o command line argument.

require 'find'

# Directory containing this script, used to locate templates (which are stored
# relative to the script itself.

SCRIPT_DIR = File.split(__FILE__)[0]
YEAR = Time.now.year.to_s

$ORG = "The Apache Software Foundation"

def read_template(file)
  result = []
 
  File.open(SCRIPT_DIR + "/" + file) do |file|
    file.each { |line| result << line }
  end
 
  return result
end

# Writes out the content (array of strings) to the file.
# Actually, writes to a temporary file, then deletes the original
# file and renames the new file to it.

def write_file(path, content)
  puts "Writing #{path} ..."
 
  temp = path + "~"
 
  File.open(temp, "w") do |file|
    content.each { |line| file << line }
  end
 
  File.delete(path)
  File.rename(temp, path)
end

# Scans the content (which should be the complete file)
# for the copyright year.  Returns the year, which
# may be as single year ("2004") or a sequence of
# years ("2004, 2005, 2007"). Returns YEAR if
# no copyright year was found in the content.

def scan_for_year(content, comment_prefix)
 
  content.each do |line|
    
    if ! line.strip.empty? then
      
      return YEAR if line[0, comment_prefix.length] != comment_prefix
      
      if line =~ /copyright ((\d+)(\s*,\s*\d+)*)/i
        then
        return $1
      end
      
    end
    
  end
 
  # Degenerate case -- a file that contains just comments?  Shouldn't happen
  # but just in case.
 
  return YEAR
 
end

# Synthesizes a copyright comment block by locating the {YEAR} token
# and substituting the year paremeter, and the {ORG} token with $ORG

def synthesize_copyright(template, year)
  template.collect { |line| line.sub(/\{YEAR\}/, year).sub(/\{ORG\}/, $ORG) }
end

class Filter

  def initialize(comment, template_file)
    @comment = comment
    @template = read_template(template_file)
  end

 def update(path)
    
    content = nil
    dirty = false
    
    File.open(path) { |file|  content = file.readlines }
    
    year = scan_for_year(content, @comment)
    
    copyright_comment = synthesize_copyright(@template, year)
    
    0.upto(@template.length() - 1) do |line|
      dirty ||= content[line] != copyright_comment[line]
    end
    
    # TODO: What if the new comment is *shorter* than the old comment?
    # Need to find and trim those line.
    
    return false if !dirty    
    
    # Strip out all leading blank lines and comments
    
    while ! content.empty?
      line = content[0]
      
      if line.strip.empty? || line[0, @comment.length] == @comment
        content.delete_at(0)
      else
        break
      end
    end
    
    # content[0] should now be the package statement (or, if in the default package,
    # an import, class, interface, etc.
    
    content.insert(0, *copyright_comment)
    
    # Write the new content to the file
    
    write_file(path, content)
    
    return true
  end
end

# Filter for Java files.  The copyright comment is placed
# before the first statement or directive (typically, before the package
# directive)

class JavaFilter < Filter
 
  def initialize
    super("//", "copyright-java.txt")
  end
 
end

class PropertiesFilter < Filter

  def initialize
    super("#", "copyright-properties.txt")
  end
end

# Filter used for any XML file.  The copyright is placed after the <?xml ...?> line, and before
# anything else.

class XMLFilter
 
  def initialize
    @template = read_template("copyright-xml.txt")
  end
 
  # Returns true if the line looks like an XML "<!DOCTYPE ..",
  # or element "<foo ..."  This will not match an XML comment
  # or the "<?xml ..." declaration.
 
  def document_start?(line)
    return line == nil || line.match(/^\s*<(!DOCTYPE|\w+)/) != nil
  end
 
  def scan_for_year(content)
    
    content.each do |line|
      if line =~ /Copyright ((\d+)(\s*,\s*\d+)*)/
        return $1
      elsif document_start?(line)
        return YEAR
      end
    end
    
    return YEAR
  end
 
  def update(path)
    
    content = nil
    dirty = false
    
    File.open(path) { |file|  content = file.readlines }
    
    year = scan_for_year(content)
    
    copyright_comment = synthesize_copyright(@template, year)
    
    # Ignore the first line, it is expected to be the <?xml
    # directive.
    
    0.upto(@template.length() - 1) do |line|
      dirty ||= content[line + 1] != copyright_comment[line]
    end
    
    # TODO: What if the new comment is *shorter* than the old comment?
    # Need to find and trim those line.
    
    return false if !dirty    
    
    until document_start?(content[1])
      content.delete_at(1)
    end
    
    content.insert(1, *copyright_comment)
    
    write_file(path, content)
    
    return true
  end
end

# Maps a particular file path pattern to a particular filter.  Tracks the files
# that have matched the pattern.

class FilterPattern
 
  def initialize(pattern, filter)
    @pattern = pattern
    @filter = filter    
    @files = []
  end
 
  def match?(path)
    if path.match(@pattern) != nil
      @files << path
      return true      
    end
    
    return false    
  end
 
  def update
    count = 0
    @files.each do |file|
      count += 1 if @filter.update(file)
    end
    
    return count
  end
end

$filter_patterns = []

def register_filter(pattern, filter)
  $filter_patterns << FilterPattern.new(pattern, filter)
end

def match?(path)
  $filter_patterns.each do |fp|
    return true if fp.match?(path)    
  end
 
  return false
end


register_filter(/\.(java|aj)$/, JavaFilter.new)
register_filter(/(\/cli\.xconf|(\.(xml|xsl|jwc|application|library|page|script)))$/, XMLFilter.new)
register_filter(/\.properties$/, PropertiesFilter.new)

$matches = 0
$update_count = 0

if (ARGV[0] == "-o")
  ARGV.shift  
  $ORG = ARGV.shift
  puts "Using organization '#$ORG'"
end

Find.find(*ARGV) do |f|
 
  if f =~ /(CVS|SVN|target)$/
    Find.prune
  else
    $matches += 1 if match?(f)
  end
end

$filter_patterns.each { |fp| $update_count += fp.update }

puts "Updated #$update_count files (of #$matches files found)."

