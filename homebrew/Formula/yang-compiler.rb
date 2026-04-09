class YangCompiler < Formula
  desc "Plugin-extensible YANG model compiler with automatic dependency resolution"
  homepage "https://github.com/yang-central/yang-compiler"
  url "https://github.com/yang-central/yang-compiler/releases/download/v1.3.1/yang-compiler-1.3.1.tar.gz"
  sha256 "PLACEHOLDER_SHA256_HASH"
  license "Apache-2.0"
  head "https://github.com/yang-central/yang-compiler.git", branch: "main"

  depends_on "maven" => :build
  depends_on "openjdk@8"

  def install
    # Build the project
    system "mvn", "clean", "package", "-DskipTests"
    
    # Install JAR and libraries
    libexec.install Dir["target/yang-compiler-*.jar"]
    libexec.install Dir["target/libs"]
    
    # Install wrapper scripts
    bin.install "yangc"
    bin.install "yangc.bat" if OS.windows?
    
    # Make yangc executable
    chmod 0755, bin/"yangc"
    
    # Create a wrapper script that sets JAVA_HOME
    (bin/"yangc").write <<~EOS
      #!/bin/bash
      export JAVA_HOME="#{Formula["openjdk@8"].opt_prefix}"
      exec "#{libexec}/yangc" "$@"
    EOS
    chmod 0755, bin/"yangc"
  end

  def caveats
    <<~EOS
      YANG Compiler requires Java 8 or higher.
      If you encounter Java-related issues, ensure JAVA_HOME is set correctly:
        export JAVA_HOME=#{Formula["openjdk@8"].opt_prefix}
    EOS
  end

  test do
    # Test help command
    assert_match "Usage:", shell_output("#{bin}/yangc --help")
    
    # Test init command
    (testpath/"test-project").mkpath
    cd testpath/"test-project" do
      system bin/"yangc", "init"
      assert_predicate testpath/"test-project/yang", :directory?
      assert_predicate testpath/"test-project/build.json", :exist?
      assert_predicate testpath/"test-project/settings.json", :exist?
    end
  end
end
