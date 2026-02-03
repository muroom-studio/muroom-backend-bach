# --------------------------------------------------------
# NAT 인스턴스 생성 및 설정
# --------------------------------------------------------
resource "aws_eip" "muroom_nat_eip" {
  domain = "vpc"

  tags = {
    Name = "muroom-nat-eip"
  }
}

resource "aws_instance" "muroom_nat_instance" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = "t4g.nano"
  subnet_id                   = aws_subnet.muroom_public_subnet_2a.id
  vpc_security_group_ids      = [aws_security_group.muroom_nat_sg.id]
  iam_instance_profile        = aws_iam_instance_profile.muroom_common_ssm_profile.name
  source_dest_check           = false
  associate_public_ip_address = true
  user_data_replace_on_change = false # user_data 변경 시 인스턴스 재생성 방지

  lifecycle {
    # ignore_changes  = [ami, user_data]
    prevent_destroy = false
  }

  user_data = <<-EOF
                #!/bin/bash
                dnf update -y

                echo "net.ipv4.ip_forward = 1" >> /etc/sysctl.conf
                sysctl -p

                dnf install -y nftables
                systemctl start nftables
                systemctl enable nftables

                PRIMARY_INTERFACE=$(ip route show 0/0 | sed -e 's/.* dev \([^ ]*\) .*/\1/')

                nft add table ip nat
                nft add chain ip nat postrouting { type nat hook postrouting priority 100 \; }
                nft add rule ip nat postrouting oifname "$PRIMARY_INTERFACE" masquerade

                # Make nftables rules persistent
                mkdir -p /etc/nftables
                nft list ruleset > /etc/nftables/nftables.rules
                echo "include \"/etc/nftables/nftables.rules\"" > /etc/sysconfig/nftables.conf

                systemctl restart nftables

                dnf install -y https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_arm64/amazon-ssm-agent.rpm
                systemctl restart amazon-ssm-agent
              EOF

  tags = {
    Name = "muroom-nat-instance"
  }
}

resource "aws_eip_association" "muroom_nat_eip_association" {
  allocation_id = aws_eip.muroom_nat_eip.id
  instance_id   = aws_instance.muroom_nat_instance.id
}

# Private Subnet을 위한 라우팅 테이블에 NAT 인스턴스를 통한 경로 추가
resource "aws_route" "muroom_private_route_via_nat_instance" {
  route_table_id         = aws_route_table.muroom_private_route_table.id
  destination_cidr_block = "0.0.0.0/0"
  network_interface_id   = aws_instance.muroom_nat_instance.primary_network_interface_id
}
