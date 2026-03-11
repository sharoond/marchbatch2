

resource "aws_instance" "example" {
  ami           = var.ami 
  instance_type = var.instance_type

  tags = {
    Name = "ExampleInstance"
  }
}

resource "aws_s3_bucket" "example_bucket" {
  count  = 2
  bucket = "example-bucket-terraform-2024-${count.index}"
  #acl    = "private"

  tags = {
    Name        = "ExampleBucket-${count.index}"
    Environment = "Dev"
  }
}